# تطبيق إدارة الموارد البشرية (HR Management App)

تطبيق حديث ومتقدم لإدارة الموارد البشرية والموظفين مبني بالكامل باستخدام **Jetpack Compose** و **Kotlin**. 
تتضمن الميزات الرئيسية:
- **لوحة التحكم (Dashboard)**: ملخص أداء المؤسسة، نسب الحضور اليومي، وإحصائيات سريعة.
- **إدارة الموظفين (Employees Screen)**: إضافة، تحرير، استعراض الموظفين مع تفاصيل المسمى الوظيفي والقسم.
- **تسجيل الحضور والغياب (Attendance Screen)**: تسجيل ساعات الحضور والانصراف وحساب الساعات تلقائيًا.
- **إدارة الفترات والورديات (Shifts Screen)**: معالجة فترات العمل (الصباحية والمسائية والدوام الكامل).
- **التقارير (Reports Screen)**: تلخيص البيانات بنسب ورسومات وتقارير شهرية/أسبوعية.

---

## كيف تجعل المشروع جاهزاً للبناء والتشغيل محلياً؟ (How to Build and Run Locally?)

لقد قمنا بتوفير كافة ملفات Gradle الأساسية وأداة تشغيل Gradle المُغلفة (**Gradle Wrapper**): `gradlew`, `gradlew.bat` وملفات الإعدادات الكاملة، مما يجعل هذا المشروع مبنياً بالكامل وجاهزاً للفتح بمجرد نقرة زر في برنامج **Android Studio**.

### الخطوة 1: تنزيل وتثبيت Android Studio
تأكد من تثبيت أحدث إصدار من بيئة التطوير **Android Studio** (إصدار Ladybug أو أحدث) على حاسوبك.

### الخطوة 2: استيراد المشروع (Import Project)
1. قم بفك الضغط عن ملف الـ ZIP الذي قمت بتنزيله من AI Studio.
2. افتح برنامج **Android Studio**.
3. اختر **Open** أو **Import Project** من القائمة.
4. تصفح للوصول إلى المجلد الرئيسي للمشروع (الذي يحتوي على ملف `settings.gradle.kts` وملف `gradlew`) واضغط **OK**.

### الخطوة 3: مزامنة Gradle (Gradle Sync)
بمجرد فتح المشروع، سيتعرف **Android Studio** تلقائياً على الملفات المُهيأة وسيقوم بإجراء مزامنة Gradle Sync تلقائياً وتنزيل الحزم المطلوبة.
> إذا لم تبدأ المزامنة تلقائياً، اضغط على زر **Sync Project with Gradle Files** (أيقونة الفيل الأزرق 🐘 في أعلى يمين الشاشة).

### الخطوة 4: تشغيل التطبيق (Run the App)
1. قم بتوصيل هاتفك الأندرويد بالكمبيوتر عبر سلك USB مع تفعيل **خيارات مطور البرامج (Developer Options)** وميزة **تصحيح أخطاء USB (USB Debugging)**.
2. أو قم بإنشاء جهاز وهمي (Virtual Device / Emulator) من خلال **Device Manager** داخل التطبيق.
3. اضغط على زر التشغيل الأخضر **Run app** (أيقونة المثلث الأخضر ▶️ في شريط الأدوات العلوي).

---

## بناء بصيغة APK يدوياً (Manual Build Options)

إذا كنت تفضل بناء التطبيق عبر سطر الأوامر (Terminal):

### على أنظمة Windows (CMD or PowerShell):
```bash
.\gradlew.bat assembleDebug
```

### على أنظمة macOS / Linux:
```bash
chmod +x gradlew
./gradlew assembleDebug
```

بعد انتهاء عملية البناء بنجاح، ستجد ملف الـ **APK** الجديد في المسار:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Build and Run English Guide

### 1. Requirements
Ensure you have the latest version of **Android Studio** (Ladybug or newer) and JDK 17+ installed.

### 2. Import to Android Studio
1. Unzip the project downloaded from AI Studio.
2. Open Android Studio and click **Open**.
3. Navigate to the root directory containing `settings.gradle.kts` and select it.

### 3. Gradle Sync
Android Studio will automatically detect the **Gradle Wrapper** configuration we generated (`gradlew` / `gradlew.bat`) and start fetching dependencies. 
If it does not start automatically, click the **Sync Project with Gradle Files** icon (the blue elephant 🐘 in the top-right toolbar).

### 4. Direct Terminal Build
You can build the debug APK directly via the terminal inside the root directory:
```bash
# Windows
.\gradlew.bat assembleDebug

# macOS/Linux
chmod +x gradlew
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

