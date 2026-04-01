const fs = require("fs");
const path = require("path");
const zlib = require("zlib");

const root = path.resolve(__dirname, "..");

const androidTargets = [
  ["composeApp/src/androidMain/res/mipmap-mdpi/ic_launcher.png", 48],
  ["composeApp/src/androidMain/res/mipmap-mdpi/ic_launcher_round.png", 48],
  ["composeApp/src/androidMain/res/mipmap-hdpi/ic_launcher.png", 72],
  ["composeApp/src/androidMain/res/mipmap-hdpi/ic_launcher_round.png", 72],
  ["composeApp/src/androidMain/res/mipmap-xhdpi/ic_launcher.png", 96],
  ["composeApp/src/androidMain/res/mipmap-xhdpi/ic_launcher_round.png", 96],
  ["composeApp/src/androidMain/res/mipmap-xxhdpi/ic_launcher.png", 144],
  ["composeApp/src/androidMain/res/mipmap-xxhdpi/ic_launcher_round.png", 144],
  ["composeApp/src/androidMain/res/mipmap-xxxhdpi/ic_launcher.png", 192],
  ["composeApp/src/androidMain/res/mipmap-xxxhdpi/ic_launcher_round.png", 192],
];

const iosTarget = ["iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png", 1024];
const previewTarget = ["build/generated/app-icon-preview.png", 512];

const colors = {
  bgTop: [27, 36, 48, 255],
  bgBottom: [10, 14, 20, 255],
  bgGlow: [61, 77, 101, 255],
  gold: [214, 162, 94, 255],
  goldSoft: [236, 198, 132, 255],
  shadow: [4, 7, 12, 180],
  route: [236, 198, 132, 128],
};

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}

function mix(a, b, t) {
  return a.map((value, index) => Math.round(value + (b[index] - value) * t));
}

function smoothstep(edge0, edge1, x) {
  const t = clamp((x - edge0) / (edge1 - edge0), 0, 1);
  return t * t * (3 - 2 * t);
}

function pointInPolygon(x, y, points) {
  let inside = false;
  for (let i = 0, j = points.length - 1; i < points.length; j = i++) {
    const xi = points[i][0];
    const yi = points[i][1];
    const xj = points[j][0];
    const yj = points[j][1];
    const intersects = ((yi > y) !== (yj > y)) &&
      (x < ((xj - xi) * (y - yi)) / ((yj - yi) || 1e-6) + xi);
    if (intersects) inside = !inside;
  }
  return inside;
}

function distanceToSegment(px, py, ax, ay, bx, by) {
  const abx = bx - ax;
  const aby = by - ay;
  const apx = px - ax;
  const apy = py - ay;
  const ab2 = abx * abx + aby * aby || 1e-6;
  const t = clamp((apx * abx + apy * aby) / ab2, 0, 1);
  const dx = px - (ax + abx * t);
  const dy = py - (ay + aby * t);
  return Math.hypot(dx, dy);
}

function distanceToPolygon(px, py, points) {
  let minDistance = Number.POSITIVE_INFINITY;
  for (let i = 0; i < points.length; i += 1) {
    const next = (i + 1) % points.length;
    minDistance = Math.min(
      minDistance,
      distanceToSegment(px, py, points[i][0], points[i][1], points[next][0], points[next][1]),
    );
  }
  return minDistance;
}

function alphaBlend(dst, src) {
  const srcAlpha = src[3] / 255;
  const dstAlpha = dst[3] / 255;
  const outAlpha = srcAlpha + dstAlpha * (1 - srcAlpha);
  if (outAlpha <= 0) return [0, 0, 0, 0];

  return [
    Math.round((src[0] * srcAlpha + dst[0] * dstAlpha * (1 - srcAlpha)) / outAlpha),
    Math.round((src[1] * srcAlpha + dst[1] * dstAlpha * (1 - srcAlpha)) / outAlpha),
    Math.round((src[2] * srcAlpha + dst[2] * dstAlpha * (1 - srcAlpha)) / outAlpha),
    Math.round(outAlpha * 255),
  ];
}

function writePng(filePath, width, height, rgbaBuffer) {
  const scanlines = Buffer.alloc((width * 4 + 1) * height);
  for (let y = 0; y < height; y += 1) {
    const scanlineOffset = y * (width * 4 + 1);
    scanlines[scanlineOffset] = 0;
    rgbaBuffer.copy(scanlines, scanlineOffset + 1, y * width * 4, (y + 1) * width * 4);
  }

  const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
  const chunks = [
    pngChunk("IHDR", Buffer.from([
      (width >>> 24) & 255, (width >>> 16) & 255, (width >>> 8) & 255, width & 255,
      (height >>> 24) & 255, (height >>> 16) & 255, (height >>> 8) & 255, height & 255,
      8, 6, 0, 0, 0,
    ])),
    pngChunk("IDAT", zlib.deflateSync(scanlines)),
    pngChunk("IEND", Buffer.alloc(0)),
  ];

  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  fs.writeFileSync(filePath, Buffer.concat([signature, ...chunks]));
}

function pngChunk(type, data) {
  const typeBuffer = Buffer.from(type);
  const lengthBuffer = Buffer.from([
    (data.length >>> 24) & 255,
    (data.length >>> 16) & 255,
    (data.length >>> 8) & 255,
    data.length & 255,
  ]);
  const crcInput = Buffer.concat([typeBuffer, data]);
  const crc = crc32(crcInput);
  const crcBuffer = Buffer.from([
    (crc >>> 24) & 255,
    (crc >>> 16) & 255,
    (crc >>> 8) & 255,
    crc & 255,
  ]);
  return Buffer.concat([lengthBuffer, typeBuffer, data, crcBuffer]);
}

const crcTable = new Uint32Array(256).map((_, index) => {
  let c = index;
  for (let k = 0; k < 8; k += 1) {
    c = (c & 1) ? (0xedb88320 ^ (c >>> 1)) : (c >>> 1);
  }
  return c >>> 0;
});

function crc32(buffer) {
  let crc = 0xffffffff;
  for (const byte of buffer) {
    crc = crcTable[(crc ^ byte) & 255] ^ (crc >>> 8);
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function createCanvas(size) {
  return Buffer.alloc(size * size * 4);
}

function setPixel(buffer, size, x, y, color) {
  if (x < 0 || y < 0 || x >= size || y >= size) return;
  const offset = (y * size + x) * 4;
  const current = [buffer[offset], buffer[offset + 1], buffer[offset + 2], buffer[offset + 3]];
  const blended = alphaBlend(current, color);
  buffer[offset] = blended[0];
  buffer[offset + 1] = blended[1];
  buffer[offset + 2] = blended[2];
  buffer[offset + 3] = blended[3];
}

function drawFilledCircle(buffer, size, cx, cy, radius, color) {
  const minX = Math.floor((cx - radius - 1) * size);
  const maxX = Math.ceil((cx + radius + 1) * size);
  const minY = Math.floor((cy - radius - 1) * size);
  const maxY = Math.ceil((cy + radius + 1) * size);
  const aa = 1.5 / size;

  for (let py = minY; py <= maxY; py += 1) {
    for (let px = minX; px <= maxX; px += 1) {
      const x = (px + 0.5) / size;
      const y = (py + 0.5) / size;
      const distance = Math.hypot(x - cx, y - cy);
      const alpha = 1 - smoothstep(radius - aa, radius + aa, distance);
      if (alpha <= 0) continue;
      setPixel(buffer, size, px, py, [color[0], color[1], color[2], Math.round(color[3] * alpha)]);
    }
  }
}

function drawRing(buffer, size, cx, cy, radius, thickness, color) {
  const minX = Math.floor((cx - radius - 1) * size);
  const maxX = Math.ceil((cx + radius + 1) * size);
  const minY = Math.floor((cy - radius - 1) * size);
  const maxY = Math.ceil((cy + radius + 1) * size);
  const inner = radius - thickness;
  const aa = 1.5 / size;

  for (let py = minY; py <= maxY; py += 1) {
    for (let px = minX; px <= maxX; px += 1) {
      const x = (px + 0.5) / size;
      const y = (py + 0.5) / size;
      const distance = Math.hypot(x - cx, y - cy);
      const outerAlpha = 1 - smoothstep(radius - aa, radius + aa, distance);
      const innerAlpha = 1 - smoothstep(inner - aa, inner + aa, distance);
      const alpha = clamp(outerAlpha - innerAlpha, 0, 1);
      if (alpha <= 0) continue;
      setPixel(buffer, size, px, py, [color[0], color[1], color[2], Math.round(color[3] * alpha)]);
    }
  }
}

function drawPolyline(buffer, size, points, thickness, color) {
  const aa = 1.5 / size;
  let minX = 1;
  let minY = 1;
  let maxX = 0;
  let maxY = 0;

  for (const [x, y] of points) {
    minX = Math.min(minX, x);
    minY = Math.min(minY, y);
    maxX = Math.max(maxX, x);
    maxY = Math.max(maxY, y);
  }

  const minPx = Math.floor((minX - thickness - 0.02) * size);
  const maxPx = Math.ceil((maxX + thickness + 0.02) * size);
  const minPy = Math.floor((minY - thickness - 0.02) * size);
  const maxPy = Math.ceil((maxY + thickness + 0.02) * size);

  for (let py = minPy; py <= maxPy; py += 1) {
    for (let px = minPx; px <= maxPx; px += 1) {
      const x = (px + 0.5) / size;
      const y = (py + 0.5) / size;
      let distance = Number.POSITIVE_INFINITY;
      for (let i = 0; i < points.length - 1; i += 1) {
        distance = Math.min(
          distance,
          distanceToSegment(x, y, points[i][0], points[i][1], points[i + 1][0], points[i + 1][1]),
        );
      }
      const alpha = 1 - smoothstep(thickness - aa, thickness + aa, distance);
      if (alpha <= 0) continue;
      setPixel(buffer, size, px, py, [color[0], color[1], color[2], Math.round(color[3] * alpha)]);
    }
  }
}

function drawPolygon(buffer, size, points, color) {
  let minX = 1;
  let minY = 1;
  let maxX = 0;
  let maxY = 0;
  for (const [x, y] of points) {
    minX = Math.min(minX, x);
    minY = Math.min(minY, y);
    maxX = Math.max(maxX, x);
    maxY = Math.max(maxY, y);
  }

  const minPx = Math.floor((minX - 0.02) * size);
  const maxPx = Math.ceil((maxX + 0.02) * size);
  const minPy = Math.floor((minY - 0.02) * size);
  const maxPy = Math.ceil((maxY + 0.02) * size);
  const aa = 1.25 / size;

  for (let py = minPy; py <= maxPy; py += 1) {
    for (let px = minPx; px <= maxPx; px += 1) {
      const x = (px + 0.5) / size;
      const y = (py + 0.5) / size;
      const inside = pointInPolygon(x, y, points);
      const distance = distanceToPolygon(x, y, points);
      const edgeAlpha = 1 - smoothstep(0, aa, distance);
      const alpha = inside ? 1 : edgeAlpha;
      if (alpha <= 0) continue;
      setPixel(buffer, size, px, py, [color[0], color[1], color[2], Math.round(color[3] * alpha)]);
    }
  }
}

function renderBackground(buffer, size) {
  for (let y = 0; y < size; y += 1) {
    for (let x = 0; x < size; x += 1) {
      const nx = x / (size - 1);
      const ny = y / (size - 1);
      const vertical = mix(colors.bgTop, colors.bgBottom, ny);
      const glow = Math.max(0, 1 - Math.hypot(nx - 0.28, ny - 0.2) / 0.55);
      const vignette = clamp(Math.hypot(nx - 0.5, ny - 0.5) / 0.72, 0, 1);
      let pixel = mix(vertical, colors.bgGlow, glow * 0.22);
      pixel = mix(pixel, colors.bgBottom, vignette * 0.18);
      const offset = (y * size + x) * 4;
      buffer[offset] = pixel[0];
      buffer[offset + 1] = pixel[1];
      buffer[offset + 2] = pixel[2];
      buffer[offset + 3] = 255;
    }
  }
}

function renderIcon(size) {
  const buffer = createCanvas(size);
  renderBackground(buffer, size);

  drawFilledCircle(buffer, size, 0.5, 0.7, 0.105, colors.shadow);

  const pinPoints = [
    [0.50, 0.80],
    [0.39, 0.56],
    [0.36, 0.47],
    [0.38, 0.39],
    [0.43, 0.32],
    [0.50, 0.29],
    [0.57, 0.32],
    [0.62, 0.39],
    [0.64, 0.47],
    [0.61, 0.56],
  ];
  drawPolygon(buffer, size, pinPoints, colors.gold);
  drawFilledCircle(buffer, size, 0.5, 0.45, 0.065, colors.bgTop);
  drawRing(buffer, size, 0.5, 0.45, 0.065, 0.01, [255, 255, 255, 42]);

  return buffer;
}

for (const [target, size] of [...androidTargets, iosTarget, previewTarget]) {
  const absoluteTarget = path.join(root, target);
  writePng(absoluteTarget, size, size, renderIcon(size));
  console.log(`Generated ${absoluteTarget}`);
}
