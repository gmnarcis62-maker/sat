package com.example.data.local

import com.example.data.local.entity.*

object InitialData {

    val satellites = listOf(
        SatelliteEntity(
            id = 1,
            name = "یاهست ۱A و ترکمن‌عالم (Yahsat 52.5°E / 52.0°E)",
            orbitalPositionDeg = 52.5,
            direction = "E",
            operator = "Al Yah Satellite Communications",
            coverage = "پوشش خاورمیانه و ایران بسیار قوی (Ku-Band MENA)",
            bands = "Ku-Band, Ka-Band",
            isFavorite = true,
            verificationDate = "1403/07",
            source = "FlySat / LyngSat"
        ),
        SatelliteEntity(
            id = 2,
            name = "هاتبرد ۱۳G/13F (Hotbird 13.0°E)",
            orbitalPositionDeg = 13.0,
            direction = "E",
            operator = "Eutelsat Communications",
            coverage = "اروپا و حوضه مدیترانه، پوشش متوسط تا خوب در غرب و مرکز ایران",
            bands = "Ku-Band",
            isFavorite = true,
            verificationDate = "1403/07",
            source = "FlySat Official"
        ),
        SatelliteEntity(
            id = 3,
            name = "یوتل‌ست ۷B/7C (Eutelsat 7.0°E)",
            orbitalPositionDeg = 7.0,
            direction = "E",
            operator = "Eutelsat Communications",
            coverage = "پرتو اختصاصی خاورمیانه و غرب آسیا (Europe B & Steerable)",
            bands = "Ku-Band",
            isFavorite = true,
            verificationDate = "1403/07",
            source = "FlySat Official"
        ),
        SatelliteEntity(
            id = 4,
            name = "بدر ۴/۵/۶/۷ (عرب‌ست Badr / Arabsat 26.0°E)",
            orbitalPositionDeg = 26.0,
            direction = "E",
            operator = "Arabsat",
            coverage = "خاورمیانه و شمال آفریقا، سیگنال قوی کانال‌های ملی و استانی IRIB",
            bands = "Ku-Band, BSS, Ka-Band",
            isFavorite = true,
            verificationDate = "1403/07",
            source = "Arabsat Technical Specs"
        ),
        SatelliteEntity(
            id = 5,
            name = "ترک‌ست ۳A/4A/5B (Türksat 42.0°E)",
            orbitalPositionDeg = 42.0,
            direction = "E",
            operator = "Türksat Satellite",
            coverage = "ترکیه، آسیای میانه و خاورمیانه (West Beam & East Beam)",
            bands = "Ku-Band, Ka-Band",
            isFavorite = false,
            verificationDate = "1403/07",
            source = "Türksat Data Sheet"
        ),
        SatelliteEntity(
            id = 6,
            name = "اینتل‌ست ۹۰۲/۳۹ (Intelsat 62.0°E)",
            orbitalPositionDeg = 62.0,
            direction = "E",
            operator = "Intelsat",
            coverage = "ایران و منطقه خلیج فارس، رله سیگنال‌های سازمان صداوسیما",
            bands = "Ku-Band, C-Band",
            isFavorite = false,
            verificationDate = "1403/07",
            source = "Intelsat Specs"
        ),
        SatelliteEntity(
            id = 7,
            name = "نایل‌ست ۲۰۱/۳۰۱ (Nilesat 7.0°W)",
            orbitalPositionDeg = -7.0,
            direction = "W",
            operator = "Nilesat Company",
            coverage = "مصر و شمال آفریقا و حاشیه خاورمیانه",
            bands = "Ku-Band",
            isFavorite = false,
            verificationDate = "1403/07",
            source = "Nilesat Technical Sheet"
        ),
        SatelliteEntity(
            id = 8,
            name = "آسترا ۱۹.۲°E (Astra 19.2°E)",
            orbitalPositionDeg = 19.2,
            direction = "E",
            operator = "SES Astra",
            coverage = "اروپای مرکزی، نیازمند دیش قطر بزرگ در شمال‌غرب ایران",
            bands = "Ku-Band",
            isFavorite = false,
            verificationDate = "1403/07",
            source = "SES Astra"
        ),
        SatelliteEntity(
            id = 9,
            name = "آذراسپیس ۱ (AzerSpace 1 46.0°E)",
            orbitalPositionDeg = 46.0,
            direction = "E",
            operator = "Azercosmos",
            coverage = "آذربایجان، قفقاز و ایران",
            bands = "Ku-Band, C-Band",
            isFavorite = false,
            verificationDate = "1403/07",
            source = "Azercosmos"
        )
    )

    val transponders = listOf(
        // Yahsat 52.5°E
        TransponderEntity(
            satelliteId = 1,
            frequencyMHz = 11881,
            polarization = "V",
            symbolRate = 27500,
            fec = "5/6",
            standard = "DVB-S QPSK",
            channels = "شبکه‌های عمومی فارسی، GEM Group, PMC, رادیو جوان",
            isStrong = true,
            status = "فعال - فرکانس مادر قوی"
        ),
        TransponderEntity(
            satelliteId = 1,
            frequencyMHz = 11996,
            polarization = "V",
            symbolRate = 27500,
            fec = "5/6",
            standard = "DVB-S QPSK",
            channels = "شبکه‌های موسیقی و سرگرمی فارسی HD",
            isStrong = true,
            status = "فعال"
        ),
        TransponderEntity(
            satelliteId = 1,
            frequencyMHz = 12015,
            polarization = "H",
            symbolRate = 27500,
            fec = "3/4",
            standard = "DVB-S2 8PSK",
            channels = "شبکه‌های خبری و فیلم فارسی HD",
            isStrong = false,
            status = "فعال"
        ),
        TransponderEntity(
            satelliteId = 1,
            frequencyMHz = 12226,
            polarization = "V",
            symbolRate = 28000,
            fec = "5/6",
            standard = "DVB-S2 8PSK",
            channels = "شبکه‌های ورزشی و استانی افغانستان و منطقه‌ای",
            isStrong = false,
            status = "فعال"
        ),

        // Hotbird 13.0°E
        TransponderEntity(
            satelliteId = 2,
            frequencyMHz = 11034,
            polarization = "V",
            symbolRate = 27500,
            fec = "3/4",
            standard = "DVB-S QPSK",
            channels = "شبکه‌های بین‌المللی و خبری (فرکانس نمونه تنظیم)",
            isStrong = true,
            status = "فعال - فرکانس نمونه هاتبرد"
        ),
        TransponderEntity(
            satelliteId = 2,
            frequencyMHz = 11565,
            polarization = "H",
            symbolRate = 29900,
            fec = "3/4",
            standard = "DVB-S2 8PSK",
            channels = "شبکه‌های اروپایی و سرگرمی",
            isStrong = false,
            status = "فعال"
        ),
        TransponderEntity(
            satelliteId = 2,
            frequencyMHz = 12149,
            polarization = "V",
            symbolRate = 27500,
            fec = "3/4",
            standard = "DVB-S QPSK",
            channels = "شبکه‌های مختلف ماهواره‌ای",
            isStrong = false,
            status = "فعال"
        ),

        // Eutelsat 7.0°E
        TransponderEntity(
            satelliteId = 3,
            frequencyMHz = 11221,
            polarization = "H",
            symbolRate = 27500,
            fec = "3/4",
            standard = "DVB-S QPSK",
            channels = "شبکه‌های Manoto, BBC Persian, Iran International",
            isStrong = true,
            status = "فعال - فرکانس مادر یوتل‌ست"
        ),
        TransponderEntity(
            satelliteId = 3,
            frequencyMHz = 11304,
            polarization = "H",
            symbolRate = 29700,
            fec = "2/3",
            standard = "DVB-S2 8PSK",
            channels = "بسته‌های پخش با وضوح بالا Full HD",
            isStrong = false,
            status = "فعال"
        ),

        // Badr 26.0°E
        TransponderEntity(
            satelliteId = 4,
            frequencyMHz = 11881,
            polarization = "H",
            symbolRate = 27500,
            fec = "5/6",
            standard = "DVB-S2 8PSK",
            channels = "بسته IRIB: شبکه‌های ۱، ۲، ۳، ۴، خبر، مستند، نسیم",
            isStrong = true,
            status = "فعال - فرکانس ملی بدر"
        ),
        TransponderEntity(
            satelliteId = 4,
            frequencyMHz = 12265,
            polarization = "H",
            symbolRate = 30000,
            fec = "3/4",
            standard = "DVB-S2 8PSK",
            channels = "شبکه‌های استانی و فراملی 4K / HD صداوسیما",
            isStrong = true,
            status = "فعال"
        ),

        // Turksat 42.0°E
        TransponderEntity(
            satelliteId = 5,
            frequencyMHz = 12380,
            polarization = "V",
            symbolRate = 27500,
            fec = "3/4",
            standard = "DVB-S QPSK",
            channels = "فرکانس شبکه سراسری TRT ترکیه و فرکانس مادر تنظیم",
            isStrong = true,
            status = "فعال - فرکانس مادر ترک‌ست"
        )
    )

    val receivers = listOf(
        ReceiverModelEntity(
            brand = "MediaStar",
            modelName = "MS-V400 / MS-15000 Forever",
            tunerType = "DVB-S2X Multi-Stream",
            cpuModel = "Ali M3521 / GX6621",
            powerSupplySpecs = "آداپتور خارجی 12V 2A با فیلتر نویز",
            commonIssues = "سوختگی رگولاتور ۱.۱ ولت هسته پردازنده، داغی بیش از حد آی‌سی تیونر، قفل روی بوت با خرابی خازن آداپتور"
        ),
        ReceiverModelEntity(
            brand = "StarSat",
            modelName = "SR-200HD Extreme 4K",
            tunerType = "DVB-S2X",
            cpuModel = "Hisilicon Hi3798MV200 Quad-core",
            powerSupplySpecs = "پاور سوئیچینگ داخلی (SMPS) با خروجی‌های 5V, 12V, 24V",
            commonIssues = "بادکردگی خازن‌های 1000uF خروجی پاور، سوختن دیود شاتکی مسیر ۱۲ ولت، خطای نرم‌افزاری Boot loop"
        ),
        ReceiverModelEntity(
            brand = "Next",
            modelName = "Next 2000 HD Mini",
            tunerType = "DVB-S2",
            cpuModel = "NationalChip GX6605S",
            powerSupplySpecs = "آداپتور 12V 1.5A",
            commonIssues = "قطع ولتاژ ۱۳/۱۸ ولت به دلیل سوختن ترانزیستور سوئیچ قطبیت تیونر S8550، پریدن پروگرام فلش هشت پایه"
        ),
        ReceiverModelEntity(
            brand = "SuperMax",
            modelName = "SuperMax SM-1x1 CHT",
            tunerType = "DVB-S2",
            cpuModel = "Ali 3606",
            powerSupplySpecs = "پاور داخلی سوئیچینگ",
            commonIssues = "تغذیه ضعیف ولتاژ ۳.۳ ولت پردازنده، قطع صدای خروجی آنالوگ"
        )
    )

    val inventory = listOf(
        InventoryItemEntity(
            name = "ال‌ان‌بی پریمیوم گلد اچ‌دی (Premium HD Gold)",
            category = "LNB",
            quantity = 12,
            minStockThreshold = 4,
            purchasePrice = 180000,
            salePrice = 280000,
            unit = "عدد"
        ),
        InventoryItemEntity(
            name = "ال‌ان‌بی دو سوزنه اینورتو بلک اولترا (Inverto Black Ultra Twin)",
            category = "LNB",
            quantity = 6,
            minStockThreshold = 2,
            purchasePrice = 450000,
            salePrice = 650000,
            unit = "عدد"
        ),
        InventoryItemEntity(
            name = "سوییچ ۴ به ۱ دایسک پریمیوم (DiSEqC 4x1 Premium)",
            category = "سوییچ",
            quantity = 15,
            minStockThreshold = 5,
            purchasePrice = 90000,
            salePrice = 160000,
            unit = "عدد"
        ),
        InventoryItemEntity(
            name = "کابل کواکسیال تمام مس RG6 (حلقه ۱۰۰ متری)",
            category = "کابل",
            quantity = 4,
            minStockThreshold = 1,
            purchasePrice = 1200000,
            salePrice = 1650000,
            unit = "حلقه"
        ),
        InventoryItemEntity(
            name = "موتور گردان دیش پریمیوم اسپیدی ۲۰۰۰ (Premium Speedy)",
            category = "موتور",
            quantity = 3,
            minStockThreshold = 1,
            purchasePrice = 3200000,
            salePrice = 4100000,
            unit = "دستگاه"
        ),
        InventoryItemEntity(
            name = "فیش F پرسی با آبکاری طلا (بسته ۱۰۰ عددی)",
            category = "اتصالات",
            quantity = 8,
            minStockThreshold = 2,
            purchasePrice = 150000,
            salePrice = 250000,
            unit = "بسته"
        ),
        InventoryItemEntity(
            name = "دیش افست ۹۰ سانتی با روکش پودری الکترواستاتیک",
            category = "دیش",
            quantity = 5,
            minStockThreshold = 2,
            purchasePrice = 750000,
            salePrice = 1100000,
            unit = "عدد"
        )
    )

    val customers = listOf(
        CustomerEntity(
            id = 1,
            fullName = "مهندس اکبری",
            phone = "09123456789",
            address = "تهران، سعادت‌آباد، میدان کاج، خ سرو غربی، پلاک ۲۲",
            notes = "ساختمان ۵ طبقه، دیش روی دکل مشترک، نیازمند سوییچ مرکزی",
            totalDebt = 0
        ),
        CustomerEntity(
            id = 2,
            fullName = "آقای رضایی",
            phone = "09351234567",
            address = "تهران، تهرانپارس، فلکه اول، خیابان گلبرگ",
            notes = "تنظیم موتور گردان و تعویض کابل پشت‌بام",
            totalDebt = 250000
        )
    )

    val missions = listOf(
        MissionEntity(
            id = 1,
            customerId = 1,
            customerName = "مهندس اکبری",
            customerPhone = "09123456789",
            missionType = "تنظیم دیش و نصب قیچی",
            status = "در حال انجام",
            scheduledDateJalali = "1403/07/15",
            priority = "فوری",
            costEstimate = 450000,
            finalFee = 450000,
            address = "سعادت‌آباد، خ سرو غربی",
            notes = "تنظیم یاهست ۵۲.۵E و نصب قیچی هاتبرد ۱۳E"
        ),
        MissionEntity(
            id = 2,
            customerId = 2,
            customerName = "آقای رضایی",
            customerPhone = "09351234567",
            missionType = "تنظیم موتور گردان",
            status = "برنامه‌ریزی‌شده",
            scheduledDateJalali = "1403/07/16",
            priority = "عادی",
            costEstimate = 600000,
            finalFee = 0,
            address = "تهرانپارس، فلکه اول",
            notes = "تنظیم صفر موتور روی جهت مداری و بررسی چرخش USALS"
        )
    )
}
