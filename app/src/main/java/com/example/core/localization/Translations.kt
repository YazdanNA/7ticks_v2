package com.example.core.localization

import androidx.compose.runtime.Composable
import com.example.ui.theme.LocalAppLanguage

object Translations {
    private val faMap = mapOf(
        // General UI & Bottom Nav
        "Smart Learn" to "یادگیری هوشمند",
        "Boxes" to "جعبه‌ها",
        "Dictionary" to "واژه‌نامه",
        "Analysis" to "تحلیل آماری",
        "Profile" to "پروفایل",
        "Settings" to "تنظیمات",
        "Back" to "بازگشت",
        "Continue" to "ادامه",
        "Cancel" to "انصراف",
        "Save" to "ذخیره",
        "Confirm" to "تایید",
        "Edit" to "ویرایش",
        "Delete" to "حذف",
        "Add" to "افزودن",
        "Search" to "جستجو",
        "Close" to "بستن",

        // Smart Learn
        "Reviews" to "مرورها",
        "Learning" to "در حال یادگیری",
        "New Words" to "کلمات جدید",
        "Mastered" to "مسلط شده",
        "Workspace" to "فضای یادگیری",
        "Today's Target:" to "هدف امروز:",
        "of" to "از",
        "mastered" to "مسلط شده",
        "Start Daily Session" to "شروع جلسه روزانه",
        "Quick Practice" to "تمرین سریع",
        "Custom Box Session" to "جلسه جعبه سفارشی",
        "Configure" to "پیکربندی",
        "Search all vocabulary..." to "جستجو در تمام واژگان...",
        "Search results for" to "نتایج جستجو برای",
        "No words found matching" to "هیچ کلمه‌ای یافت نشد برای",

        // Profile
        "User Profile" to "پروفایل کاربری",
        "Level" to "سطح",
        "Target Language" to "زبان هدف",
        "Daily Goal" to "هدف روزانه",
        "Reminder Time" to "زمان یادآوری",
        "Stats Overview" to "نمای کلی آمار",
        "Learning Progress" to "پیشرفت یادگیری",
        "Streak Days" to "روزهای پیوسته",
        "Words Practiced" to "کلمات تمرین شده",
        "FSRS Retention Rate" to "نرخ ماندگاری FSRS",
        "Weekly Activity" to "فعالیت هفتگی",
        "Native Language" to "زبان مادری",
        "Notification Time" to "زمان اعلان",
        "App Preferences" to "تنظیمات برنامه",
        "Dark Mode" to "حالت تاریک",
        "Change Avatar" to "تغییر آواتار",
        "Edit Profile" to "ویرایش پروفایل",
        "Enter name..." to "نام خود را وارد کنید...",

        // Analysis
        "Cognitive Retention Analysis" to "تحلیل ماندگاری شناختی",
        "Learning Overview" to "نمای کلی یادگیری",
        "Weekly Activity Profile" to "پروفایل فعالیت هفتگی",
        "FSRS Retention Index" to "شاخص ماندگاری FSRS",
        "Words Mastered over Time" to "کلمات مسلط شده به مرور زمان",
        "Total Words" to "کل کلمات",
        "Active Cards" to "کارت‌های فعال",
        "Retention" to "ماندگاری",
        "Daily Streak" to "توالی روزانه",
        "Stability" to "پایداری",
        "Difficulty" to "دشواری",

        // Boxes Screen
        "SuperLeitner Spaced Repetition Engine" to "موتور تکرار فاصله‌دار سوپر‌لایتنر",
        "Total" to "کل",
        "Cards" to "کارت‌ها",
        "Due" to "سررسید",
        "New" to "جدید",
        "Empty Box" to "جعبه خالی",
        "Review Due Cards" to "مرور کارت‌های سررسید شده",
        "Start Box Session" to "شروع جلسه این جعبه",
        "View Cards" to "مشاهده کارت‌ها",
        "Box" to "جعبه",

        // Learning Session & Flashcards
        "Easy" to "آسان",
        "Good" to "خوب",
        "Hard" to "سخت",
        "Again" to "دوباره",
        "Show Answer" to "نمایش پاسخ",
        "FSRS Brain Spacing In progress..." to "محاسبه فواصل مغزی FSRS در جریان است...",
        "Session Completed!" to "جلسه با موفقیت به پایان رسید!",
        "Words Reviewed" to "کلمات مرور شده",
        "Mastered Words Added" to "کلمات مسلط شده اضافه شده",
        "Back to Dashboard" to "بازگشت به داشبورد",
        "Keep Learning" to "ادامه یادگیری",
        "Swipe left to mark Again, swipe right to mark Good." to "برای نشانه دوباره به چپ، برای نشانه خوب به راست بکشید.",
        "Swipe up to show answer." to "برای نمایش پاسخ به بالا بکشید.",

        // Dialogs & Exit
        "Exit Session?" to "خروج از جلسه تعاملی؟",
        "Are you sure you want to exit the learning session? Progress for currently active card will not be saved." to "آیا مطمئن هستید که می‌خواهید از جلسه یادگیری خارج شوید؟ پیشرفت کارت فعال فعلی ذخیره نخواهد شد.",
        "Exit" to "خروج",
        "Stay" to "ماندن",
        "Are you sure you want to exit SevenTicks?" to "آیا مطمئن هستید که می‌خواهید از سون‌تیکز خارج شوید؟",
        "Are you sure you want to exit the application?" to "آیا مطمئن هستید که می‌خواهید از برنامه خارج شوید؟",
        "Exit App" to "خروج از برنامه",
        "Yes" to "بله",
        "No" to "خیر",

        // Tiki Mascots Default Dialogue
        "Hi there! I am Tiki, your vocabulary mentor." to "سلام! من تیکی هستم، مربی واژگان شما.",
        "Welcome! Let's level up your vocabulary today!" to "خوش آمدید! بیایید امروز سطح واژگان شما را ارتقا دهیم!"
    )

    fun translate(text: String, lang: String): String {
        if (lang != "fa") return text
        
        // 1. Direct exact map lookup
        val exactMatch = faMap[text.trim()]
        if (exactMatch != null) return exactMatch

        // 2. Partial template translation
        var result = text
        faMap.forEach { (eng, fa) ->
            if (eng.length > 3) { // Only replace significant phrases
                result = result.replace(eng, fa, ignoreCase = true)
            }
        }
        
        // Translate common dynamic formats or punctuation
        if (lang == "fa") {
            result = result
                .replace("Hi, ", "سلام، ")
                .replace("Today's Target: ", "هدف امروز: ")
                .replace(" of ", " از ")
                .replace(" mastered", " مسلط شده")
                .replace("Workspace", "فضای کاری")
                .replace("Level ", "سطح ")
                .replace("Box ", "جعبه ")
                .replace(" due", " سررسید شده")
                .replace(" due cards", " کارت سررسید شده")
                .replace("Due Cards: ", "کارت‌های سررسید شده: ")
                .replace("New Words: ", "کلمات جدید: ")
        }

        return result
    }
}

@Composable
fun String.localize(): String {
    val lang = LocalAppLanguage.current
    return Translations.translate(this, lang)
}
