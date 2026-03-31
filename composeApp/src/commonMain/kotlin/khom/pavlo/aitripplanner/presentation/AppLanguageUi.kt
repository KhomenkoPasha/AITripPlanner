package khom.pavlo.aitripplanner.presentation

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.AppSyncState
import khom.pavlo.aitripplanner.domain.model.Budget
import khom.pavlo.aitripplanner.domain.model.CompanionType
import khom.pavlo.aitripplanner.domain.model.Interest
import khom.pavlo.aitripplanner.domain.model.Pace
import khom.pavlo.aitripplanner.domain.model.TravelMode
import khom.pavlo.aitripplanner.domain.model.TripPreference
import kotlin.math.abs
import kotlin.math.roundToInt

internal fun AppLanguage.daysLabel(count: Int): String = when (this) {
    AppLanguage.EN -> "$count days"
    AppLanguage.RU -> "$count дней"
    AppLanguage.UK -> "$count днів"
}

internal fun AppLanguage.dayLabel(index: Int): String = when (this) {
    AppLanguage.EN -> "Day $index"
    AppLanguage.RU -> "День $index"
    AppLanguage.UK -> "День $index"
}

internal fun AppLanguage.placesLabel(count: Int): String = when (this) {
    AppLanguage.EN -> "$count places"
    AppLanguage.RU -> "$count точек"
    AppLanguage.UK -> "$count місць"
}

internal fun AppLanguage.pendingSyncLabel(): String = when (this) {
    AppLanguage.EN -> "Pending sync"
    AppLanguage.RU -> "Ожидает синхронизации"
    AppLanguage.UK -> "Очікує синхронізації"
}

internal fun AppLanguage.syncedLabel(): String = when (this) {
    AppLanguage.EN -> "Synced"
    AppLanguage.RU -> "Синхронизировано"
    AppLanguage.UK -> "Синхронізовано"
}

internal fun AppLanguage.offlineDraftLabel(): String = when (this) {
    AppLanguage.EN -> "Offline draft"
    AppLanguage.RU -> "Локальный черновик"
    AppLanguage.UK -> "Локальна чернетка"
}

internal fun AppLanguage.cloudSyncedLabel(): String = when (this) {
    AppLanguage.EN -> "Cloud synced"
    AppLanguage.RU -> "Синхронизировано с облаком"
    AppLanguage.UK -> "Синхронізовано з хмарою"
}

internal fun AppLanguage.easyPaceLabel(): String = when (this) {
    AppLanguage.EN -> "Easy pace"
    AppLanguage.RU -> "Спокойный темп"
    AppLanguage.UK -> "Спокійний темп"
}

internal fun AppLanguage.balancedPaceLabel(): String = when (this) {
    AppLanguage.EN -> "Balanced pace"
    AppLanguage.RU -> "Сбалансированный темп"
    AppLanguage.UK -> "Збалансований темп"
}

internal fun AppLanguage.syncingChangesLabel(count: Int): String = when (this) {
    AppLanguage.EN -> "Syncing $count changes"
    AppLanguage.RU -> "Синхронизация $count изменений"
    AppLanguage.UK -> "Синхронізація $count змін"
}

internal fun AppLanguage.syncPausedLabel(): String = when (this) {
    AppLanguage.EN -> "Sync paused"
    AppLanguage.RU -> "Синхронизация приостановлена"
    AppLanguage.UK -> "Синхронізацію призупинено"
}

internal fun AppLanguage.queuedChangesLabel(count: Int): String = when (this) {
    AppLanguage.EN -> "$count changes queued"
    AppLanguage.RU -> "В очереди $count изменений"
    AppLanguage.UK -> "У черзі $count змін"
}

internal fun AppLanguage.everythingSyncedLabel(): String = when (this) {
    AppLanguage.EN -> "Everything synced"
    AppLanguage.RU -> "Все синхронизировано"
    AppLanguage.UK -> "Усе синхронізовано"
}

internal fun AppLanguage.hoursLabel(hours: Int, minutes: Int): String = when (this) {
    AppLanguage.EN -> "${hours}h ${minutes}m"
    AppLanguage.RU -> "${hours}ч ${minutes}м"
    AppLanguage.UK -> "${hours}год ${minutes}хв"
}

internal fun AppLanguage.minutesLabel(minutes: Int): String = when (this) {
    AppLanguage.EN -> "${minutes}m"
    AppLanguage.RU -> "${minutes}м"
    AppLanguage.UK -> "${minutes}хв"
}

internal fun AppLanguage.distanceLabel(distanceKm: Double): String {
    val value = distanceKm.toOneDecimalString()
    return when (this) {
        AppLanguage.EN -> "$value km"
        AppLanguage.RU -> "$value км"
        AppLanguage.UK -> "$value км"
    }
}

internal fun AppLanguage.tripNotFoundError(): String = when (this) {
    AppLanguage.EN -> "Trip not found"
    AppLanguage.RU -> "Маршрут не найден"
    AppLanguage.UK -> "Маршрут не знайдено"
}

internal fun AppLanguage.deletePlaceError(): String = when (this) {
    AppLanguage.EN -> "Unable to delete place"
    AppLanguage.RU -> "Не удалось удалить точку"
    AppLanguage.UK -> "Не вдалося видалити точку"
}

internal fun AppLanguage.updatePlaceStatusError(): String = when (this) {
    AppLanguage.EN -> "Unable to update place status"
    AppLanguage.RU -> "Не удалось обновить статус точки"
    AppLanguage.UK -> "Не вдалося оновити статус точки"
}

internal fun AppLanguage.deleteTripError(): String = when (this) {
    AppLanguage.EN -> "Unable to delete trip"
    AppLanguage.RU -> "Не удалось удалить маршрут"
    AppLanguage.UK -> "Не вдалося видалити маршрут"
}

internal fun AppLanguage.requiredPlannerFieldsError(): String = when (this) {
    AppLanguage.EN -> "City, title, days, places, walking minutes, and request are required"
    AppLanguage.RU -> "Город, название, дни, точки, минуты пешком и запрос обязательны"
    AppLanguage.UK -> "Місто, назва, дні, місця, хвилини пішки та запит обов'язкові"
}

internal fun AppLanguage.invalidDaysError(): String = when (this) {
    AppLanguage.EN -> "Days must be a whole number greater than 0"
    AppLanguage.RU -> "Количество дней должно быть целым числом больше 0"
    AppLanguage.UK -> "Кількість днів має бути цілим числом більше 0"
}

internal fun AppLanguage.invalidPlaceCountError(): String = when (this) {
    AppLanguage.EN -> "Place count must be a whole number greater than 0"
    AppLanguage.RU -> "Количество точек должно быть целым числом больше 0"
    AppLanguage.UK -> "Кількість місць має бути цілим числом більше 0"
}

internal fun AppLanguage.invalidWalkingMinutesError(): String = when (this) {
    AppLanguage.EN -> "Walking minutes per day must be a whole number greater than 0"
    AppLanguage.RU -> "Минуты пешком в день должны быть целым числом больше 0"
    AppLanguage.UK -> "Хвилини пішки на день мають бути цілим числом більше 0"
}

internal fun AppLanguage.saveTripError(): String = when (this) {
    AppLanguage.EN -> "Unable to save trip"
    AppLanguage.RU -> "Не удалось сохранить маршрут"
    AppLanguage.UK -> "Не вдалося зберегти маршрут"
}

internal fun AppLanguage.noStopsPlannedYetLabel(): String = when (this) {
    AppLanguage.EN -> "No stops planned yet."
    AppLanguage.RU -> "Точки пока не запланированы."
    AppLanguage.UK -> "Точки поки не заплановані."
}

internal fun AppLanguage.placeNotFoundError(): String = when (this) {
    AppLanguage.EN -> "Place not found"
    AppLanguage.RU -> "Точка не найдена"
    AppLanguage.UK -> "Точку не знайдено"
}

internal fun AppLanguage.placeCompletedStatusLabel(): String = when (this) {
    AppLanguage.EN -> "Visited"
    AppLanguage.RU -> "Пройдено"
    AppLanguage.UK -> "Пройдено"
}

internal fun AppLanguage.placePlannedStatusLabel(): String = when (this) {
    AppLanguage.EN -> "Planned"
    AppLanguage.RU -> "Запланировано"
    AppLanguage.UK -> "Заплановано"
}

internal fun AppLanguage.stopLabel(position: Int, total: Int): String = when (this) {
    AppLanguage.EN -> "Stop $position of $total"
    AppLanguage.RU -> "Точка $position из $total"
    AppLanguage.UK -> "Місце $position із $total"
}

internal fun AppLanguage.bestTimeLabel(position: Int, total: Int): String {
    val slot = when {
        total <= 1 -> when (this) {
            AppLanguage.EN -> "Flexible time"
            AppLanguage.RU -> "Гибкое время"
            AppLanguage.UK -> "Гнучкий час"
        }
        position <= total / 3 -> when (this) {
            AppLanguage.EN -> "Morning"
            AppLanguage.RU -> "Утро"
            AppLanguage.UK -> "Ранок"
        }
        position >= total -> when (this) {
            AppLanguage.EN -> "Evening"
            AppLanguage.RU -> "Вечер"
            AppLanguage.UK -> "Вечір"
        }
        else -> when (this) {
            AppLanguage.EN -> "Midday"
            AppLanguage.RU -> "День"
            AppLanguage.UK -> "День"
        }
    }
    return when (this) {
        AppLanguage.EN -> "Best time: $slot"
        AppLanguage.RU -> "Лучшее время: $slot"
        AppLanguage.UK -> "Найкращий час: $slot"
    }
}

internal fun AppLanguage.categoryLabel(category: String?): String? = category
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.replace('_', ' ')
    ?.replace('-', ' ')
    ?.split(' ')
    ?.joinToString(" ") { token ->
        token.lowercase().replaceFirstChar { char -> char.uppercase() }
    }

internal fun AppLanguage.priceLevelLabel(priceLevel: String?): String? = when (priceLevel?.trim()?.lowercase()) {
    "free" -> when (this) {
        AppLanguage.EN -> "Free"
        AppLanguage.RU -> "Бесплатно"
        AppLanguage.UK -> "Безкоштовно"
    }
    "ticketed" -> when (this) {
        AppLanguage.EN -> "Ticketed"
        AppLanguage.RU -> "По билету"
        AppLanguage.UK -> "За квитком"
    }
    "premium" -> when (this) {
        AppLanguage.EN -> "Premium"
        AppLanguage.RU -> "Премиум"
        AppLanguage.UK -> "Преміум"
    }
    else -> priceLevel?.takeIf { it.isNotBlank() }
}

internal fun AppLanguage.openingStatusLabel(isOpenNow: Boolean?): String? = when (isOpenNow) {
    true -> when (this) {
        AppLanguage.EN -> "Open now"
        AppLanguage.RU -> "Сейчас открыто"
        AppLanguage.UK -> "Зараз відчинено"
    }
    false -> when (this) {
        AppLanguage.EN -> "Closed now"
        AppLanguage.RU -> "Сейчас закрыто"
        AppLanguage.UK -> "Зараз зачинено"
    }
    null -> null
}

internal fun AppLanguage.placeAboutText(
    placeName: String,
    address: String,
    note: String,
    shortDescription: String,
    fullDescription: String,
): String = when {
    fullDescription.isNotBlank() -> fullDescription
    shortDescription.isNotBlank() -> shortDescription
    note.isNotBlank() -> note
    else -> when (this) {
        AppLanguage.EN -> "$placeName is one of the calm route stops near $address."
        AppLanguage.RU -> "$placeName входит в спокойный маршрут и расположен рядом с $address."
        AppLanguage.UK -> "$placeName входить до спокійного маршруту та розташований поруч із $address."
    }
}

internal fun AppLanguage.placeWhyInRouteText(
    placeName: String,
    dayTitle: String,
    position: Int,
    total: Int,
    whyIncluded: String,
): String = when {
    whyIncluded.isNotBlank() -> whyIncluded
    else -> when (this) {
        AppLanguage.EN -> "$placeName supports \"$dayTitle\" and is planned as stop $position of $total to keep the day balanced and walkable."
        AppLanguage.RU -> "$placeName поддерживает тему \"$dayTitle\" и стоит точкой $position из $total, чтобы день оставался сбалансированным и удобным для прогулки."
        AppLanguage.UK -> "$placeName підтримує тему \"$dayTitle\" і стоїть зупинкою $position із $total, щоб день залишався збалансованим і зручним для прогулянки."
    }
}

internal fun AppLanguage.placeTipsText(
    tips: List<String>,
    visitNotes: String,
    visitTimeLabel: String,
    address: String,
    hasPhoto: Boolean,
): String {
    val items = buildList {
        addAll(tips.filter { it.isNotBlank() })
        if (visitNotes.isNotBlank()) add(visitNotes)
    }
    if (items.isNotEmpty()) {
        return items.joinToString(separator = "\n") { "- $it" }
    }

    return when (this) {
        AppLanguage.EN -> buildString {
            append("Plan around $visitTimeLabel for this stop. ")
            append("Use $address as the arrival point. ")
            append(if (hasPhoto) "The current photo can be reused later for a richer gallery." else "Photo gallery is ready for richer media when backend images expand.")
        }
        AppLanguage.RU -> buildString {
            append("Закладывайте около $visitTimeLabel на эту точку. ")
            append("Используйте $address как ориентир для прибытия. ")
            append(if (hasPhoto) "Текущее фото можно позже расширить до полноценной галереи." else "Блок галереи уже готов для будущих фото из backend.")
        }
        AppLanguage.UK -> buildString {
            append("Закладайте близько $visitTimeLabel на цю точку. ")
            append("Використовуйте $address як орієнтир прибуття. ")
            append(if (hasPhoto) "Поточне фото можна пізніше розширити до повноцінної галереї." else "Блок галереї вже готовий для майбутніх фото з backend.")
        }
    }
}

internal fun AppLanguage.placeVisitDetailsText(
    openingHoursText: String,
    websiteUrl: String?,
    neighborhood: String,
): String = buildString {
    if (openingHoursText.isNotBlank()) {
        append(
            when (this@placeVisitDetailsText) {
                AppLanguage.EN -> "Hours: $openingHoursText"
                AppLanguage.RU -> "Часы работы: $openingHoursText"
                AppLanguage.UK -> "Години роботи: $openingHoursText"
            },
        )
    }
    if (neighborhood.isNotBlank()) {
        if (isNotEmpty()) append('\n')
        append(
            when (this@placeVisitDetailsText) {
                AppLanguage.EN -> "Neighborhood: $neighborhood"
                AppLanguage.RU -> "Район: $neighborhood"
                AppLanguage.UK -> "Район: $neighborhood"
            },
        )
    }
    if (!websiteUrl.isNullOrBlank()) {
        if (isNotEmpty()) append('\n')
        append(
            when (this@placeVisitDetailsText) {
                AppLanguage.EN -> "Website: $websiteUrl"
                AppLanguage.RU -> "Сайт: $websiteUrl"
                AppLanguage.UK -> "Сайт: $websiteUrl"
            },
        )
    }
}

internal fun TravelMode.label(language: AppLanguage): String = when (this) {
    TravelMode.WALKING -> when (language) {
        AppLanguage.EN -> "Walking"
        AppLanguage.RU -> "Пешком"
        AppLanguage.UK -> "Пішки"
    }
    TravelMode.CAR -> when (language) {
        AppLanguage.EN -> "Car"
        AppLanguage.RU -> "На машине"
        AppLanguage.UK -> "На машині"
    }
    TravelMode.PUBLIC_TRANSPORT -> when (language) {
        AppLanguage.EN -> "Public transport"
        AppLanguage.RU -> "Общественный транспорт"
        AppLanguage.UK -> "Громадський транспорт"
    }
    TravelMode.TAXI -> when (language) {
        AppLanguage.EN -> "Taxi"
        AppLanguage.RU -> "Такси"
        AppLanguage.UK -> "Таксі"
    }
}

internal fun Interest.label(language: AppLanguage): String = when (this) {
    Interest.MUSEUMS -> when (language) {
        AppLanguage.EN -> "Museums"
        AppLanguage.RU -> "Музеи"
        AppLanguage.UK -> "Музеї"
    }
    Interest.HISTORY -> when (language) {
        AppLanguage.EN -> "History"
        AppLanguage.RU -> "История"
        AppLanguage.UK -> "Історія"
    }
    Interest.FOOD -> when (language) {
        AppLanguage.EN -> "Food"
        AppLanguage.RU -> "Еда"
        AppLanguage.UK -> "Їжа"
    }
    Interest.CAFE -> when (language) {
        AppLanguage.EN -> "Cafes"
        AppLanguage.RU -> "Кафе"
        AppLanguage.UK -> "Кафе"
    }
    Interest.VIEWS -> when (language) {
        AppLanguage.EN -> "Views"
        AppLanguage.RU -> "Виды"
        AppLanguage.UK -> "Краєвиди"
    }
    Interest.ARCHITECTURE -> when (language) {
        AppLanguage.EN -> "Architecture"
        AppLanguage.RU -> "Архитектура"
        AppLanguage.UK -> "Архітектура"
    }
    Interest.NIGHTLIFE -> when (language) {
        AppLanguage.EN -> "Nightlife"
        AppLanguage.RU -> "Ночная жизнь"
        AppLanguage.UK -> "Нічне життя"
    }
    Interest.NATURE -> when (language) {
        AppLanguage.EN -> "Nature"
        AppLanguage.RU -> "Природа"
        AppLanguage.UK -> "Природа"
    }
}

internal fun Pace.label(language: AppLanguage): String = when (this) {
    Pace.RELAXED -> when (language) {
        AppLanguage.EN -> "Relaxed"
        AppLanguage.RU -> "Спокойный"
        AppLanguage.UK -> "Спокійний"
    }
    Pace.NORMAL -> when (language) {
        AppLanguage.EN -> "Normal"
        AppLanguage.RU -> "Нормальный"
        AppLanguage.UK -> "Звичайний"
    }
    Pace.INTENSIVE -> when (language) {
        AppLanguage.EN -> "Intensive"
        AppLanguage.RU -> "Интенсивный"
        AppLanguage.UK -> "Інтенсивний"
    }
}

internal fun Budget.label(language: AppLanguage): String = when (this) {
    Budget.BUDGET -> when (language) {
        AppLanguage.EN -> "Budget"
        AppLanguage.RU -> "Экономно"
        AppLanguage.UK -> "Економно"
    }
    Budget.MEDIUM -> when (language) {
        AppLanguage.EN -> "Medium"
        AppLanguage.RU -> "Средний"
        AppLanguage.UK -> "Середній"
    }
    Budget.PREMIUM -> when (language) {
        AppLanguage.EN -> "Premium"
        AppLanguage.RU -> "Премиум"
        AppLanguage.UK -> "Преміум"
    }
}

internal fun CompanionType.label(language: AppLanguage): String = when (this) {
    CompanionType.COUPLE -> when (language) {
        AppLanguage.EN -> "For couple"
        AppLanguage.RU -> "Для пары"
        AppLanguage.UK -> "Для пари"
    }
    CompanionType.SOLO -> when (language) {
        AppLanguage.EN -> "Solo"
        AppLanguage.RU -> "Соло"
        AppLanguage.UK -> "Соло"
    }
}

internal fun TripPreference.label(language: AppLanguage): String = when (this) {
    TripPreference.MUST_SEE -> when (language) {
        AppLanguage.EN -> "Must-see"
        AppLanguage.RU -> "Топ места"
        AppLanguage.UK -> "Топ місця"
    }
    TripPreference.LOCAL_SPOTS -> when (language) {
        AppLanguage.EN -> "Local spots"
        AppLanguage.RU -> "Локальные места"
        AppLanguage.UK -> "Локальні місця"
    }
    TripPreference.HIDDEN_GEMS -> when (language) {
        AppLanguage.EN -> "Hidden gems"
        AppLanguage.RU -> "Скрытые места"
        AppLanguage.UK -> "Приховані місця"
    }
    TripPreference.NO_CROWDS -> when (language) {
        AppLanguage.EN -> "No crowds"
        AppLanguage.RU -> "Без толпы"
        AppLanguage.UK -> "Без натовпу"
    }
    TripPreference.NO_RUSH -> when (language) {
        AppLanguage.EN -> "No rush"
        AppLanguage.RU -> "Без спешки"
        AppLanguage.UK -> "Без поспіху"
    }
    TripPreference.SUNSET -> when (language) {
        AppLanguage.EN -> "Sunset"
        AppLanguage.RU -> "На закате"
        AppLanguage.UK -> "На заході сонця"
    }
    TripPreference.RAINY_DAY -> when (language) {
        AppLanguage.EN -> "Rainy day"
        AppLanguage.RU -> "Дождливый день"
        AppLanguage.UK -> "Дощовий день"
    }
    TripPreference.SHORT_ROUTE -> when (language) {
        AppLanguage.EN -> "Short route"
        AppLanguage.RU -> "Короткий маршрут"
        AppLanguage.UK -> "Короткий маршрут"
    }
    TripPreference.FREE_PLACES -> when (language) {
        AppLanguage.EN -> "Free places"
        AppLanguage.RU -> "Бесплатные места"
        AppLanguage.UK -> "Безкоштовні місця"
    }
    TripPreference.INSTAGRAM_SPOTS -> when (language) {
        AppLanguage.EN -> "Instagram spots"
        AppLanguage.RU -> "Instagram места"
        AppLanguage.UK -> "Instagram місця"
    }
}

internal fun AppSyncState.toStatusLabel(language: AppLanguage): String? = when {
    isRunning -> language.syncingChangesLabel(queuedItems)
    lastError != null -> language.syncPausedLabel()
    queuedItems > 0 -> language.queuedChangesLabel(queuedItems)
    lastCompletedAtEpochMillis != null -> language.everythingSyncedLabel()
    else -> null
}

private fun Double.toOneDecimalString(): String {
    val scaled = (this * 10.0).roundToInt()
    val whole = scaled / 10
    val fraction = abs(scaled % 10)
    return "$whole.$fraction"
}
