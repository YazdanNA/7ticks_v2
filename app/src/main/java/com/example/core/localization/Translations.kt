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

        // Challenges Screen
        "Active Challenges" to "چالش‌های فعال",
        "Active Challenge" to "چالش فعال",
        "Earn Extra XP Points" to "کسب امتیاز تجربه (XP) اضافی",
        "No active challenges found" to "هیچ چالش فعالی یافت نشد",
        "Tap to view all challenges" to "برای مشاهده همه چالش‌ها ضربه بزنید",
        "Quests synchronize in real-time with the central FSRS engine" to "ماموریت‌ها به صورت آنی با موتور مرکزی FSRS همگام‌سازی می‌شوند",
        "Daily Reviewer" to "مرورگر روزانه",
        "Weekly Reviewer" to "مرورگر هفتگی",
        "FSRS Calibration" to "کالیبراسیون FSRS",
        "Double Session" to "جلسه دوگانه",
        "XP Power Hour" to "ساعت قدرت تجربه",
        "Vocabulary Expander" to "توسعه‌دهنده واژگان",
        "Persistent Mind" to "ذهن مستمر",
        "XP Marathon" to "ماراتن تجربه",
        "Review 10 words today." to "امروز ۱۰ واژه را مرور کنید.",
        "Perform reviews on 5 different days." to "مرور کلمات را در ۵ روز مختلف انجام دهید.",
        "Review 20 words today to calibrate spaced retention." to "امروز ۲۰ واژه را مرور کنید تا سیستم فاصله تکرار کالیبره شود.",
        "Complete 2 separate adaptive study sessions." to "۲ جلسه یادگیری تطبیقی مجزا را کامل کنید.",
        "Earn 150 total XP today." to "امروز در مجموع ۱۵۰ امتیاز تجربه کسب کنید.",
        "Introduce and learn 50 brand new words." to "۵۰ واژه جدید را معرفی کرده و یاد بگیرید.",
        "Achieve or maintain a 5-day active learning streak." to "یک دوره یادگیری فعال ۵ روزه را کسب یا حفظ کنید.",
        "Acquire 1000 total XP within this week." to "در طول این هفته در مجموع ۱۰۰۰ امتیاز تجربه کسب کنید.",
        "YOU'RE DONE FOR TODAY" to "کار شما برای امروز تمام شد!",
        "CONTINUE LEARNING" to "ادامه یادگیری",
        "START STUDY SESSION" to "شروع جلسه یادگیری",

        // Onboarding Wizard Texts
        "Hi there! I am Tiki, your vocabulary mentor. Welcome to 7Ticks, a scientifically proven spaced repetition system!" to "سلام! من تیکی هستم، مربی واژگان شما. به سون‌تیکز خوش آمدید، یک سیستم تکرار فاصله‌دار که از نظر علمی اثبات شده است!",
        "Let's get to know you! What is your name?" to "بیا بیشتر با هم آشنا بشیم! نام شما چیست؟",
        "Choose an avatar that fits your style! It will represent your cognitive profile." to "یک آواتار متناسب با سلیقه خود انتخاب کنید! این آواتار نمایه شناختی شما را نشان خواهد داد.",
        "Select your native language so I can customize translations and learning aids." to "زبان مادری خود را انتخاب کنید تا بتوانم ترجمه‌ها و ابزارهای کمکی یادگیری را سفارشی‌سازی کنم.",
        "Select the target language you want to master!" to "زبان هدفی که می‌خواهید بر آن مسلط شوید را انتخاب کنید!",
        "How much time would you like to dedicate to learning daily? Let's build a strong habit." to "روزانه چقدر زمان می‌خواهید به یادگیری اختصاص دهید؟ بیایید یک عادت قوی بسازیم.",
        "Set a daily reminder time so we don't break your learning streak!" to "یک زمان یادآوری روزانه تنظیم کنید تا توالی یادگیری شما قطع نشود!",
        "Let's run a short adaptive assessment to determine your starting vocabulary level!" to "بیایید یک ارزیابی تطبیقی کوتاه برای تعیین سطح واژگان شروع شما انجام دهیم!",
        "Excellent job! I have fully mapped your starting neural vocabulary profile." to "کار شما عالی بود! من نمایه واژگان عصبی اولیه شما را به طور کامل نقشه‌برداری کردم.",
        "I'm assessing your skill with every answer. Focus up, you've got this!" to "من با هر پاسخ مهارت شما را ارزیابی می‌کنم. تمرکز کنید، شما می‌توانید!",
        "Woohoo! Your local environment setup is perfect. Let's begin our mastery journey!" to "هورا! راه‌اندازی محیط محلی شما عالی است. بیایید سفر تسلط خود را شروع کنیم!",
        "Oh no, there was an error in initializing the database. Let's retry." to "اوه نه، خطایی در راه‌اندازی پایگاه داده رخ داد. بیایید دوباره تلاش کنیم.",
        "Setting up SevenTicks... Downloading your dynamic language database now." to "در حال راه‌اندازی سون‌تیکز... در حال دانلود پایگاه داده پویای زبان شما.",
        
        "Welcome" to "خوش‌آمدگویی",
        "Your Name" to "نام شما",
        "Select Avatar" to "انتخاب آواتار",
        "Native Language" to "زبان مادری",
        "Target Language" to "زبان هدف",
        "Daily Commitment" to "تعهد روزانه",
        "Reminder Notification" to "اعلان یادآوری",
        "Placement Assessment" to "سنجش تعیین سطح",
        "Finalizing Setup" to "نهایی‌سازی راه‌اندازی",
        "Getting everything ready..." to "در حال آماده‌سازی همه‌چیز...",
        "Preparing search indices..." to "در حال آماده‌سازی شاخص‌های جستجو...",
        "Initializing profile indices..." to "در حال آماده‌سازی شاخص‌های پروفایل...",
        "Finishing details..." to "در حال اتمام جزئیات...",
        "Please select your native language" to "لطفاً زبان مادری خود را انتخاب کنید",
        "Please select your target language" to "لطفاً زبان هدف خود را انتخاب کنید",
        "Enter your name" to "نام خود را وارد کنید",
        "Let's Begin" to "شروع کنیم",
        "Select Time" to "انتخاب زمان",
        "Daily Notification" to "اعلان روزانه",
        "Choose Notification Time" to "زمان اعلان را انتخاب کنید",
        "Adaptive Placement Test" to "آزمون تعیین سطح تطبیقی",
        "Completed" to "تکمیل شد",
        "Start Mastery" to "شروع تسلط",

        // Smart Learn Screen Items
        "Today's Target:" to "هدف امروز:",
        "Start Daily Session" to "شروع جلسه روزانه",
        "Quick Practice" to "تمرین سریع",
        "Custom Box Session" to "جلسه جعبه سفارشی",
        "Configure" to "پیکربندی",
        "Search all vocabulary..." to "جستجو در تمام واژگان...",
        "Search results for" to "نتایج جستجو برای",
        "No words found matching" to "هیچ کلمه‌ای یافت نشد برای",

        // Dictionary Screen Items
        "Explore word structures, synonyms, and levels! Star terms to find them quickly." to "ساختار کلمات، مترادف‌ها و سطوح را کاوش کنید! اصطلاحات را ستاره‌دار کنید تا سریع‌تر آن‌ها را بیابید.",
        "in my database. Let's try typing another term or check the spelling!" to "در پایگاه داده‌ام. بیایید اصطلاح دیگری را تایپ کرده یا املاء آن را بررسی کنید!",
        "Synonyms" to "مترادف‌ها",
        "Antonyms" to "متضادها",
        "Examples" to "مثال‌ها",
        "Definition" to "تعریف",
        "Translations" to "ترجمه‌ها",
        "Word Family" to "هم‌خانواده کلمات",
        "Collocations" to "ترکیب‌های کلمات",
        "Phrases" to "عبارت‌ها",
        "Notes" to "یادداشت‌ها",
        "Part of Speech" to "نقش کلمه",
        "Pronounce" to "تلفظ",

        // Boxes Screen Items
        "You don't have any archived boxes yet." to "شما هنوز هیچ جعبه آرشیو شده‌ای ندارید.",
        "Let's create your first custom vocabulary collection! Tap 'Create Box' above." to "بیایید اولین مجموعه واژگان سفارشی خود را بسازیم! روی 'ایجاد جعبه' در بالا ضربه بزنید.",
        "No words inside this box matched your search. Tap '+' to create/auto-fill new terms!" to "هیچ کلمه‌ای در این جعبه با جستجوی شما مطابقت نداشت. روی '+' برای ایجاد یا تکمیل خودکار کلمات جدید ضربه بزنید!",
        "Superb job! You finished reviewing words inside " to "کار عالی! شما مرور کلمات درون این جعبه را به پایان رساندید ",
        "! Your memory is now highly calibrated!" to "! حافظه شما اکنون بسیار کالیبره شده است!",
        "Create Box" to "ایجاد جعبه",
        "Import Box" to "وارد کردن جعبه",
        "Custom Boxes" to "جعبه‌های سفارشی",
        "My Vocab Boxes" to "جعبه‌های واژگان من",
        "Personal study collections" to "مجموعه‌های مطالعه شخصی",
        "Edit / Rename" to "ویرایش / تغییر نام",
        "Unarchive" to "خروج از آرشیو",
        "Archive" to "آرشیو کردن",
        "Duplicate" to "تکثیر",
        "Export JSON Backup" to "خروجی نسخه پشتیبان JSON",
        "Delete Permanently" to "حذف دائمی",
        "Save Changes" to "ذخیره تغییرات",
        "Return to Box" to "بازگشت به جعبه",
        "Parse & Restore Box" to "تجزیه و بازیابی جعبه",
        "Save Word Changes" to "ذخیره تغییرات کلمه",
        "Add Word to Box" to "افزودن کلمه به جعبه",
        "Active Vocabulary Boxes" to "جعبه‌های واژگان فعال",
        "Auto-Fill from Dictionary Source" to "تکمیل خودکار از منبع واژه‌نامه",
        "Accent Color" to "رنگ برجسته",
        "Select Accent Color" to "رنگ برجسته را انتخاب کنید",
        "Box Name" to "نام جعبه",
        "Enter box name..." to "نام جعبه را وارد کنید...",
        "Box Description" to "توضیحات جعبه",
        "Enter box description..." to "توضیحات جعبه را وارد کنید...",
        "Select Box Icon" to "آیکون جعبه را انتخاب کنید",
        "Auto-Fill Word" to "تکمیل خودکار کلمه",
        "Enter English word to auto-fill..." to "کلمه انگلیسی را جهت تکمیل خودکار وارد کنید...",
        "Word Schema Fields" to "فیلدهای ساختار کلمه",
        "Paste the copied vocabulary box backup JSON text here. We will recreate the box and restore all elements." to "متن پشتیبان JSON کپی شده جعبه واژگان را در اینجا قرار دهید. ما جعبه را دوباره ایجاد کرده و تمام عناصر را بازیابی خواهیم کرد.",
        "Smart Learn Session" to "جلسه یادگیری هوشمند",

        // Analysis Screen Items
        "Overview" to "نمای کلی",
        "Cognitive" to "شناختی",
        "Activity" to "فعالیت",
        "Simulator" to "شبیه‌ساز",
        "Establish a daily review routine to discover your optimal learning hours." to "یک روال مرور روزانه ایجاد کنید تا ساعت‌های یادگیری بهینه خود را کشف کنید.",
        "Memory retention dropped slightly recently. Prioritize scheduled reviews before introducing more brand-new words." to "ماندگاری حافظه اخیراً کمی کاهش یافته است. قبل از معرفی کلمات جدید، مرورهای زمان‌بندی‌شده را در اولویت قرار دهید.",
        "Retention is highly stable. Daily consistency will further secure these words in long-term memory." to "ماندگاری کلمات بسیار پایدار است. استمرار روزانه این کلمات را در حافظه بلندمدت تثبیت می‌کند.",
        "XP boost! Complete daily challenges and maintain a high correct answer ratio to level up player stats." to "امتیاز افزایشی (XP)! چالش‌های روزانه را تکمیل کنید و نسبت پاسخ‌های درست را بالا نگه دارید تا سطح خود را ارتقا دهید.",
        "Your performance is best with shorter, concentrated sessions of 10 to 15 cards to avoid brain fatigue." to "عملکرد شما با جلسات کوتاه‌تر و متمرکز ۱۰ تا ۱۵ کارتی برای جلوگیری از خستگی مغز، در بهترین حالت خود خواهد بود.",
        "Study consistently to generate personalized learning intelligence insights." to "برای تولید تحلیل‌های هوشمند شخصی‌سازی شده، به طور مستمر مطالعه کنید.",
        "You study most efficiently in the morning." to "شما صبح‌ها بیشترین بازدهی یادگیری را دارید.",
        "You study most efficiently in the evening." to "شما عصرها بیشترین بازدهی یادگیری را دارید.",
        "of your reviews are completed during those hours." to "از مرورهای شما در این ساعات تکمیل می‌شود.",

        // Profile Screen Items
        "Let's master some words today to unlock your first spaced repetition milestone!" to "بیا امروز روی چند کلمه مسلط بشیم تا اولین نقطه عطف تکرار فاصله‌دار خودمون رو باز کنیم!",
        "You are making stellar progress, " to "پیشرفت فوق‌العاده‌ای داری، ",
        "! You have already unlocked " to "! تو از قبل ",
        " achievements." to " دستاورد باز کردی.",

        // Smart Learn Screen Items
        "Welcome back, " to "خوش برگشتی، ",
        "! 👋 Ready for another cognitive boost?" to "! 👋 آماده یک تقویت شناختی دیگر هستی؟",
        "You have an active spacing session waiting!" to "تو یک جلسه تکرار فعال منتظر داری!",
        "Your neural pathways are highly receptive right now!" to "مسیرهای عصبی تو در حال حاضر بسیار پذیرا هستند!",
        "Daily quests are active! Let's conquer some academic words!" to "ماموریت‌های روزانه فعال هستند! بیا چند کلمه دانشگاهی را تسخیر کنیم!",

        // Study Session & Flashcards Reactions
        "Tiki is celebrating your cognitive growth! Let's do a happy island dance! 🌴💃" to "تیکی در حال جشن گرفتن رشد شناختی شماست! بیایید رقص شاد جزیره‌ای انجام دهیم! 🌴💃",
        "Tiki is proud of your persistence! Double high-five! 🐾✋" to "تیکی به پشتکار شما افتخار می‌کند! بزن قدش دو دستی! 🐾✋",
        "Tiki is amazed by your dedication! That is a rare badge! 🏆✨" to "تیکی از فداکاری شما شگفت‌زده شده است! این یک نشان نادر است! 🏆✨",
        "Your burning passion is inspiring! Tiki says keep the flame alive! 🔥🦖" to "اشتیاق سوزان شما الهام‌بخش است! تیکی می‌گوید شعله را زنده نگه دارید! 🔥🦖",
        "Tiki is cheering for you! Keep up the incredible work!" to "تیکی در حال تشویق شماست! به کار فوق‌العاده خود ادامه دهید!",
        "Tiki is watching! Recall correctly to impress me!" to "تیکی در حال تماشا است! برای تحت تاثیر قرار دادن من، به درستی یادآوری کنید!",

        // Dialogues from dialogues_en.json
        "A new word. Please do not feed the vocabulary." to "یک کلمه جدید. لطفا به این واژه غذا ندهید!",
        "First contact with an extremely introverted word." to "اولین برخورد با یک کلمه به شدت درون‌گرا.",
        "A fresh term appears. Approach with mild curiosity." to "یک اصطلاح تازه ظاهر شد. با کنجکاوی ملایم نزدیک شوید.",
        "Introducing a word that dislikes bright lights." to "معرفی کلمه‌ای که از نورهای روشن بیزار است.",
        "First meeting. It is polite to look interested." to "اولین ملاقات. باادبانه است که علاقه‌مند به نظر برسید.",
        "An unfamiliar word. It seems friendly enough." to "یک کلمه ناآشنا. به اندازه کافی دوستانه به نظر می‌رسد.",
        "A new linguistic specimen has wandered in." to "یک نمونه زبان‌شناختی جدید وارد شده است.",
        "Meet a word that refuses to be ignored." to "ملاقات با کلمه‌ای که از نادیده گرفته شدن امتناع می‌کند.",
        "First contact. The word is sizing you up." to "اولین برخورد. کلمه دارد شما را برانداز می‌کند.",
        "An unexpected guest in our review deck today." to "یک مهمان غیرمنتظره در دسته مرور امروز ما.",
        "A quiet introduction to a very private noun." to "یک معرفی آرام برای یک اسم بسیار خصوصی.",
        "This word has arrived without an invitation." to "این کلمه بدون دعوت وارد شده است.",
        "A new concept. Handle with light curiosity." to "یک مفهوم جدید. با کنجکاوی ملایم با آن برخورد کنید.",
        "First contact. This term is surprisingly quiet." to "اولین برخورد. این اصطلاح به طرز شگفت‌آوری آرام است.",
        "An unusual word. It is still adjusting." to "یک کلمه غیرمعمول. هنوز در حال سازگاری است.",
        "Greeting a word that recently arrived here." to "خوش‌آمدگویی به کلمه‌ای که به تازگی به اینجا رسیده است.",
        "First encounter. No sudden movements, please." to "اولین ملاقات. لطفاً حرکت ناگهانی انجام ندهید!",
        "A fresh term has quietly joined us." to "یک اصطلاح جدید بی‌سروصدا به ما پیوسته است.",
        "Meet a word with a very specific attitude." to "ملاقات با کلمه‌ای با یک نگرش بسیار خاص.",
        "First contact. This node seems slightly confused." to "اولین برخورد. این بخش کمی گیج به نظر می‌رسد.",
        "A new verb. It is highly experimental." to "یک فعل جدید. بسیار آزمایشی است.",
        "First meeting. The word seems slightly defensive." to "اولین ملاقات. کلمه کمی دفاعی به نظر می‌رسد.",
        "A rare word has entered your hemisphere." to "یک کلمه نادر وارد نیمکره مغزی شما شده است.",
        "An unfamiliar term. Let's keep our distance." to "یک اصطلاح ناآشنا. بیایید فاصله‌مان را حفظ کنیم.",
        "A new vocabulary node. Mildly intriguing indeed." to "یک گره واژگانی جدید. در واقع تا حدی جذاب است.",
        "First contact. The letters are still settling." to "اولین برخورد. حروف هنوز در حال نشستن هستند.",
        "An unexpected word joins today's quiet circle." to "یک کلمه غیرمعومت به حلقه آرام امروز می‌پیوندد.",
        "Introducing a term that prefers quiet corners." to "معرفی اصطلاحی که گوشه‌های دنج و ساکت را ترجیح می‌دهد.",
        "A fresh node. It smells like paper." to "یک گره تازه. بوی کاغذ می‌دهد!",
        "First meeting with a rather curious adjective." to "اولین ملاقات با یک صفت نسبتاً کنجکاو.",
        "A new word. Try not to startle it." to "یک کلمه جدید. سعی کنید آن را نترسانید!",
        "First contact. The spelling is highly intentional." to "اولین برخورد. املای کلمه کاملاً هدفمند است.",
        "An interesting term. Let us observe quietly." to "یک اصطلاح جالب. بیایید بی‌سروصدا مشاهده کنیم.",
        "First meeting. The word nods back silently." to "اولین ملاقات. کلمه بی‌صدا سر تکان می‌دهد.",
        "A new arrival. It seems quite stable." to "یک تازه وارد. کاملاً پایدار به نظر می‌رسد.",
        "Trying this word again. Third time is charm." to "تلاش دوباره برای این کلمه. تا سه نشه بازی نشه!",
        "Another attempt. The word feels very familiar." to "یک تلاش دیگر. کلمه بسیار آشنا به نظر می‌رسد.",
        "We meet again. Did you miss me?" to "دوباره همدیگر را ملاقات کردیم. دلت برایم تنگ شده بود؟",
        "Back so soon? This word has gravity." to "به این زودی برگشتی؟ این کلمه نیروی جاذبه دارد!",
        "A repeat attempt. Let's try a smile." to "یک تلاش مجدد. بیایید یک لبخند را امتحان کنیم.",
        "Revisiting this term. It has missed you." to "بازبینی این اصطلاح. دلش برای شما تنگ شده بود!",
        "Attempt number two. Let us keep it simple." to "تلاش شماره دو. بیایید ساده نگهش داریم.",
        "Back here again. The word looks different." to "دوباره برگشتیم اینجا. کلمه متفاوت به نظر می‌رسد.",
        "Another try. The letters haven't changed." to "یک تلاش دیگر. حروف تغییر نکرده‌اند.",
        "Meeting this word again. A quiet reunion." to "ملاقات دوباره با این کلمه. یک تجدید دیدار آرام.",
        "Re-attempting. Let's pretend it's the first time." to "تلاش دوباره. بیایید وانمود کنیم بار اول است!",
        "This word again. It has a persistent nature." to "دوباره این کلمه. ماهیت بسیار سمجی دارد!",
        "Another encounter. The letters are staying put." to "ملاقاتی دیگر. حروف در جای خود باقی مانده‌اند.",
        "Back for seconds. The vocabulary is rich." to "برگشتیم برای بار دوم. دایره واژگان غنی است!",
        "A repeat try. Let's negotiate with memory." to "یک تلاش مجدد. بیایید با حافظه مذاکره کنیم!",
        "Once more. The meaning remains the same." to "یک بار دیگر. معنی کلمه همان است که بود.",
        "Re-examining. The definition is still holding on." to "امتحان مجدد. تعریف هنوز پابرجاست.",
        "Back to this card. A persistent little node." to "بازگشت به این کارت. یک گره کوچک سمج!",
        "A second glance. The word stands its ground." to "نگاهی دوباره. کلمه سر حرفش ایستاده است.",
        "Another round. Memory is playing gentle games." to "یک دور دیگر. حافظه دارد بازی‌های ظریفی می‌کند.",
        "Revisiting. The word is waiting patiently here." to "بازبینی مجدد. کلمه صبورانه اینجا منتظر است.",
        "Back here. Let's try a softer touch." to "برگشتیم اینجا. بیایید با ملایمت بیشتری برخورد کنیم.",
        "A repeat attempt. No pressure, just letters." to "یک تلاش مجدد. فشاری نیست، فقط چند تا حرفه!",
        "Once again. The term greets you quietly." to "یک بار دیگر. این اصطلاح بی‌سروصدا به شما سلام می‌گوید.",
        "Re-attempting this. Let's keep our focus." to "تلاش مجدد برای این کلمه. بیایید تمرکزمان را حفظ کنیم.",
        "Another try. The definition is still stable." to "یک تلاش دیگر. تعریف هنوز ثابت است.",
        "Back to this concept. A familiar path." to "بازگشت به این مفهوم. یک مسیر آشنا.",
        "Frequently Forgotten Words" to "کلماتی که بیشتر فراموش شده‌اند",
        "No memory lapses registered. Excellent recall precision!" to "هیچ فراموشی ثبت نشده است. دقت یادآوری فوق‌العاده است!",
        "Most Difficult Words" to "دشوارترین کلمات",
        "No complex intervals yet. FSRS parameters are fully balanced." to "هنوز فواصل پیچیده‌ای وجود ندارد. پارامترهای FSRS کاملاً متعادل هستند.",
        "Most Repeated Words" to "پرتکرارترین کلمات",
        "No statistics collected. Initiate reviews to map repetition counts." to "هیچ آماری جمع‌آوری نشده است. برای ثبت دفعات تکرار، مرور کلمات را آغاز کنید.",
        "Less  " to "کمتر  ",
        "  More" to "  بیشتر",
        "Total Study" to "کل مطالعه",
        "Today Study" to "مطالعه امروز",
        "Weekly Study" to "مطالعه هفتگی",
        "Current XP" to "تجربه فعلی",
        "Next Level" to "سطح بعدی",
        "Avg Session" to "میانگین جلسه",
        "Vocabulary Deck Size" to "اندازه دسته واژگان",
        "Simulation Duration" to "مدت زمان شبیه‌سازی",
        "Daily Study Session" to "جلسه مطالعه روزانه",
        "Student Success Rate (Recall Prob)" to "نرخ موفقیت زبان‌آموز (احتمال یادآوری)",
        "Vocabulary Placement Test" to "آزمون تعیین سطح واژگان",
        "Custom Vocabulary Boxes & Spacing" to "جعبه‌های واژگان سفارشی و فواصل تکرار",
        "Custom Box Leitner Distribution Profile" to "پروفایل توزیع لایتنر جعبه سفارشی",
        "Day 1" to "روز ۱",
        "Interval Spacing Smoothing Curve (FSRS + Leitner)" to "منحنی هموارسازی فواصل تکرار (FSRS + لایتنر)",
        "Copy Report" to "کپی کردن گزارش",
        "Share Report" to "اشتراک‌گذاری گزارش",
        "SmartSessionEngine Simulator" to "شبیه‌ساز موتور جلسه هوشمند",
        "This utility runs an ultra-fast in-memory simulation of the complete Leitner box and FSRS v4.5 spacing engine, strictly using the SmartSessionEngine prioritization and capacity rules. Customize the parameters below to run the simulation." to "این ابزار یک شبیه‌سازی درون‌حافظه‌ای بسیار سریع از جعبه کامل لایتنر و موتور فاصله تکرار FSRS v4.5 را با استفاده از قوانین اولویت‌بندی و ظرفیت موتور جلسه هوشمند اجرا می‌کند. پارامترهای زیر را برای اجرای شبیه‌سازی سفارشی کنید.",
        "Simulation Settings" to "تنظیمات شبیه‌سازی",
        "Simulation Diagnostics Results" to "نتایج تشخیص شبیه‌سازی",
        "Sim Reviews" to "مرورهای شبیه‌سازی شده",
        "Matured (Box 7)" to "تثبیت‌شده (جعبه ۷)",
        "Avg Daily Queue" to "میانگین صف روزانه",
        "Peak Workload" to "اوج حجم کاری",
        "Virtual User Journey & Activity (Complete App)" to "مسیر و فعالیت کاربر مجازی (کل برنامه)",
        "CEFR Placed" to "سطح CEFR تعیین شده",
        "Database Segregation Verified" to "تأیید جداسازی پایگاه داده",
        "Independent SQLite tracking. Custom Box words and Smart Learn progress do not overlap or interfere under any conditions." to "ردیابی مستقل در SQLite. کلمات جعبه سفارشی و پیشرفت یادگیری هوشمند تحت هیچ شرایطی با هم همپوشانی یا تداخل ندارند.",
        "Algorithmic Integrity Verification" to "تأیید یکپارچگی الگوریتمی",
        "Workload Distribution Over Time (Reviews/Day)" to "توزیع حجم کاری در طول زمان (مرور/روز)",
        "Final Box Distribution Profile" to "پروفایل توزیع نهایی جعبه",
        "Full Mathematical Report File" to "فایل کامل گزارش ریاضی",
        "Learning Overview" to "نمای کلی یادگیری",
        "Learning Intelligence" to "هوش یادگیری",
        "FSRS & Cognitive Mastery Insights" to "بینش‌های تسلط شناختی و FSRS",
        "Adaptive Goal Tracking" to "ردیابی هدف تطبیقی",
        "CEFR Level Progression" to "روند پیشرفت سطح CEFR",
        "Locked" to "قفل شده",
        "Smart Analytics Insights" to "بینش‌های تحلیل هوشمند",
        "Memory Retention" to "ماندگاری حافظه",
        "Accuracy" to "دقت",
        "Review Rating Analytics" to "تحلیل رتبه‌بندی مرورها",
        "Leitner Seven Circles of Learning" to "هفت دایره یادگیری لایتنر",
        "Leitner Box 1 contains brand-new reviews, scaling up to Box 7 (fully mastered vocabulary). Click on a circle to review." to "جعبه لایتنر ۱ شامل مرورهای کاملاً جدید است که تا جعبه ۷ (واژگان کاملاً تسلط‌یافته) افزایش می‌یابد. برای مرور روی یک دایره کلیک کنید.",
        "Vocabulary Topic Analysis" to "تحلیل موضوعی واژگان",
        "Weak Word Analytics" to "تحلیل کلمات ضعیف",
        "Weekly Activity Profile" to "پروفایل فعالیت هفتگی",
        "Words Reviewed Per Day" to "کلمات مرور شده در روز",
        "Cognitive Study Consistency Heatmap" to "نقشه حرارتی استمرار مطالعه شناختی",
        "Mapping active cognitive reviews over the last 12 weeks. Scroll horizontally." to "ترسیم مرورهای شناختی فعال در ۱۲ هفته گذشته. به صورت افقی اسکرول کنید.",
        "Time & XP Intelligence" to "هوش زمان و امتیاز تجربه",
        "Export Intelligence Reports" to "خروجی گزارش‌های هوشمند",
        "Secure local copies or share structured progress diagnostics with your study partners." to "نسخه‌های محلی امن تهیه کنید یا تشخیص‌های پیشرفت ساختاریافته را با شرکای مطالعه خود به اشتراک بگذارید.",
        "Generate PDF Progress Report" to "ایجاد گزارش پیشرفت PDF",
        "Export Vocabulary Deck (CSV)" to "خروجی دسته واژگان (CSV)",
        "Copy Markdown Progress Summary" to "کپی خلاصه پیشرفت به زبان مارک‌داون",
        "Circle" to "دایره",
        "Circle 1" to "دایره ۱",
        "Circle 2" to "دایره ۲",
        "Circle 3" to "دایره ۳",
        "Circle 4" to "دایره ۴",
        "Circle 5" to "دایره ۵",
        "Circle 6" to "دایره ۶",
        "Circle 7" to "دایره ۷",
        "Tiki is celebrating your cognitive growth! Let's do a happy island dance! 🌴💃" to "تیکی در حال جشن گرفتن رشد شناختی شماست! بیایید رقص شاد جزیره‌ای انجام دهیم! 🌴💃",
        "Tiki is proud of your persistence! Double high-five! 🐾✋" to "تیکی به پشتکار شما افتخار می‌کند! بزن قدش دو دستی! 🐾✋",
        "Tiki is amazed by your dedication! That is a rare badge! 🏆✨" to "تیکی از فداکاری شما شگفت‌زده شده است! این یک نشان نادر است! 🏆✨",
        "Your burning passion is inspiring! Tiki says keep the flame alive! 🔥🦖" to "اشتیاق سوزان شما الهام‌بخش است! تیکی می‌گوید شعله را زنده نگه دارید! 🔥🦖",
        "Tiki is cheering for you! Keep up the incredible work!" to "تیکی در حال تشویق شماست! به کار فوق‌العاده خود ادامه دهید!",
        "Tiki is watching! Recall correctly to impress me!" to "تیکی در حال تماشا است! برای تحت تاثیر قرار دادن من، به درستی یادآوری کنید!",
        "YOU'RE DONE FOR TODAY" to "کار شما برای امروز تمام شد!",
        "CONTINUE LEARNING" to "ادامه یادگیری",
        "START STUDY SESSION" to "شروع جلسه یادگیری",
        "Active Challenge" to "چالش فعال",
        "Relearn" to "یادگیری مجدد",
        "Learn" to "یادگیری",
        "STREAK BURNING! 🔥" to "استریک داغ! 🔥",
        "Days" to "روز",
        "KEEP BURNING! 🔥" to "ادامه با قدرت! 🔥"
    )

    fun translate(text: String, lang: String): String {
        if (lang != "fa") return text
        
        // 1. Direct exact map lookup
        val exactMatch = faMap[text.trim()]
        if (exactMatch != null) return exactMatch

        // 2. Partial template translation
        var result = text
        
        // Sort keys by length descending to match longer strings/templates first
        val sortedKeys = faMap.keys.sortedByDescending { it.length }
        for (eng in sortedKeys) {
            if (eng.length > 3) { // Only replace significant phrases
                val fa = faMap[eng] ?: continue
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
                .replace("CEFR Level ", "سطح CEFR ")
                .replace("CEFR Level: ", "سطح CEFR: ")
                .replace("Learned: ", "یاد گرفته شده: ")
                .replace(" / Mastered: ", " / مسلط شده: ")
                .replace("Streak Freeze Spells: ", "طلسم‌های محافظ استریک: ")
                .replace(" reviews (", " مرور (")
                .replace("Interval ", "فاصله ")
                .replace(" / Total: ", " / کل: ")
                .replace(" words", " کلمه")
                .replace(" Years (", " سال (")
                .replace(" Days)", " روز)")
                .replace(" Days", " روز")
                .replace(" minutes/day", " دقیقه/روز")
                .replace("Score: ", "امتیاز: ")
                .replace(" adaptive questions (IRT)", " سوال تطبیقی (IRT)")
                .replace("Created: ", "ایجاد شده: ")
                .replace(" Personal Boxes", " جعبه شخصی")
                .replace("Added: ", "اضافه شده: ")
                .replace(" custom words", " کلمه سفارشی")
                .replace("Reviews: ", "مرورها: ")
                .replace("% Recall", "% یادآوری")
                .replace("Day ", "روز ")
        }

        return result
    }
}

@Composable
fun String.localize(): String {
    val lang = LocalAppLanguage.current
    return Translations.translate(this, lang)
}

