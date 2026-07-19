package com.example.a5120_shademate.data

import com.example.a5120_shademate.model.ContentType
import com.example.a5120_shademate.model.EducationCategory
import com.example.a5120_shademate.model.EducationContent
import com.example.a5120_shademate.model.HeatLevel
import com.example.a5120_shademate.model.HeatMapData
import com.example.a5120_shademate.model.HeatZone
import kotlinx.coroutines.delay

enum class MockMode {
    SUCCESS,
    EMPTY,
    ERROR,
}

/**
 * Fake repository for Iteration 1 UI work.
 * Now populated with real data from the education API.
 */
class MockHeatRepository(
    private val heatMapMode: MockMode = MockMode.SUCCESS,
    private val educationMode: MockMode = MockMode.SUCCESS,
    private val simulatedDelayMillis: Long = 1000L,
) : HeatRepository {

    override suspend fun getHeatMapData(): HeatMapData {
        delay(simulatedDelayMillis)
        return when (heatMapMode) {
            MockMode.SUCCESS -> sampleHeatMapData()
            MockMode.EMPTY -> HeatMapData("Melbourne", "Just now", emptyList())
            MockMode.ERROR -> error("Mock heat map request failed.")
        }
    }

    override suspend fun getEducationContent(): List<EducationContent> {
        delay(simulatedDelayMillis)
        return when (educationMode) {
            MockMode.SUCCESS -> sampleEducationContent()
            MockMode.EMPTY -> emptyList()
            MockMode.ERROR -> error("Mock education request failed.")
        }
    }

    private fun sampleHeatMapData(): HeatMapData {
        return HeatMapData(
            cityName = "Melbourne",
            lastUpdated = "2 mins ago",
            zones = listOf(
                HeatZone("cbd", "CBD", 20, HeatLevel.EXTREME, 0.52f, 0.42f, "Dense urban heat around the city core."),
                HeatZone("footscray", "Footscray", 17, HeatLevel.HOT, 0.28f, 0.44f, "Hot conditions across western suburbs."),
                HeatZone("brunswick", "Brunswick", 13, HeatLevel.WARM, 0.48f, 0.24f, "Warm afternoon temperatures continuing north."),
                HeatZone("st-kilda", "St Kilda", 19, HeatLevel.COOL, 0.60f, 0.72f, "Cooler coastal air near Port Phillip Bay."),
                HeatZone("dandenong", "Dandenong", 20, HeatLevel.HOT, 0.84f, 0.58f, "Sustained heat across the south-east corridor.")
            )
        )
    }

    private fun sampleEducationContent(): List<EducationContent> {
        return listOf(
            EducationContent(1, "70,345 People Live in Melbourne CBD Heat Zones", ContentType.STATISTIC, EducationCategory.VULNERABILITY, "70,345 residents live across Melbourne CBD heat zones - all exposed to extreme urban heat every summer.", "ABS 2021 Census", "https://www.abs.gov.au/census/find-census-data/datapacks", "2021-01-01"),
            EducationContent(2, "Melbourne CBD Population Surged +74% in 10 Years", ContentType.STATISTIC, EducationCategory.VULNERABILITY, "CBD population jumped +74% from 2011 to 2021. More people than ever are exposed to urban heat risk.", "ABS 2021 Census", "https://www.abs.gov.au/census/find-census-data/datapacks", "2021-01-01"),
            EducationContent(3, "4,027 Elderly Residents in Melbourne CBD Heat Zones", ContentType.STATISTIC, EducationCategory.VULNERABILITY, "4,027 people aged 65+ live in Melbourne CBD heat zones. Elderly residents face the highest risk of heat illness.", "ABS 2021 Census", "https://www.abs.gov.au/census/find-census-data/datapacks", "2021-01-01"),
            EducationContent(4, "East Melbourne: 1 in 5 Residents is Elderly", ContentType.STATISTIC, EducationCategory.VULNERABILITY, "22.3% of East Melbourne residents are aged 65+. This neighbourhood has the highest elderly heat-risk rate in the CBD.", "ABS 2021 Census", "https://www.abs.gov.au/census/find-census-data/datapacks", "2021-01-01"),
            EducationContent(5, "4,040 Children Under 14 Live in Melbourne CBD Heat Zones", ContentType.STATISTIC, EducationCategory.VULNERABILITY, "4,040 children under 14 live in CBD heat zones. Children cannot regulate body temperature as effectively as adults.", "ABS 2021 Census", "https://www.abs.gov.au/census/find-census-data/datapacks", "2021-01-01"),
            EducationContent(6, "2,786 Outdoor Workers Face Heat Exposure Daily in Melbourne CBD", ContentType.STATISTIC, EducationCategory.VULNERABILITY, "2,786 workers in heat-exposed industries operate in Melbourne CBD. Outdoor workers face heat danger every shift.", "ABS 2021 Census", "https://www.abs.gov.au/census/find-census-data/datapacks", "2021-01-01"),
            EducationContent(7, "1,361 Construction Workers Work Outdoors in CBD Heat", ContentType.STATISTIC, EducationCategory.VULNERABILITY, "1,361 construction workers work in Melbourne CBD with no escape from direct sun and extreme heat.", "ABS 2021 Census", "https://www.abs.gov.au/census/find-census-data/datapacks", "2021-01-01"),
            EducationContent(8, "18.4% of CBD Workers Are in Physical Occupations", ContentType.STATISTIC, EducationCategory.VULNERABILITY, "18.4% of employed CBD residents are labourers, tradespeople or machine operators - all high heat-exposure occupations.", "ABS 2021 Census", "https://www.abs.gov.au/census/find-census-data/datapacks", "2021-01-01"),
            EducationContent(9, "Heatwaves Kill More Than All Other Natural Disasters Combined", ContentType.FACT, EducationCategory.CLIMATE_RECORD, "Since 1900, heatwaves have killed more Australians than floods, fires and storms combined. Heat is Australia's deadliest hazard.", "Coates et al. (2014)", "https://www.sciencedirect.com/science/article/pii/S1462901114000999", "2014-01-01"),
            EducationContent(10, "374 Deaths in Victoria's 2009 Heatwave", ContentType.STATISTIC, EducationCategory.HEALTH_IMPACT, "374 people died in Victoria's 2009 heatwave. Extreme heat kills - never ignore a heat warning.", "Victorian Government", "https://knowledge.aidr.org.au/media/4442/jan-2009-victoria-heatwave-assessment-of-health-impacts.pdf", "2009-01-01"),
            EducationContent(11, "167 Deaths in Victoria's 2014 Heatwave", ContentType.STATISTIC, EducationCategory.HEALTH_IMPACT, "167 lives were lost in the 2014 heatwave in just a few days. Melbourne summer heat is life-threatening.", "Victorian Government", "https://knowledge.aidr.org.au/resources/health-heatwave-south-eastern-australia-2009/", "2014-01-01"),
            EducationContent(12, "15,000+ Victorians Hospitalised by Heat Each Year", ContentType.STATISTIC, EducationCategory.HEALTH_IMPACT, "15,000+ Victorians end up in hospital from heat every year. Know your risk before you step outside.", "Monash University", "https://ehjournal.biomedcentral.com/articles/10.1186/s12940-026-01289-5", "2026-03-20"),
            EducationContent(13, "Climate Change Pushed Heat Hospitalisations Up 27%", ContentType.STATISTIC, EducationCategory.HEALTH_IMPACT, "Climate change has driven heat hospitalisations up 27% - and it's getting worse every summer.", "Monash University", "https://ehjournal.biomedcentral.com/articles/10.1186/s12940-026-01289-5", "2026-03-20"),
            EducationContent(14, "7,104 Heat Hospitalisations in Australia Over 10 Years", ContentType.STATISTIC, EducationCategory.HEALTH_IMPACT, "7,104 Australians hospitalised from heat in one decade. Heat illness is far more common than you think.", "AIHW", "https://www.aihw.gov.au/reports/injury/extreme-weather-injuries/contents/extreme-heat", "2023-11-02"),
            EducationContent(15, "Most Heatwave Deaths Occur Inside Homes", ContentType.FACT, EducationCategory.VULNERABILITY, "Most heatwave deaths happen indoors. Being inside does not mean you are safe from heat.", "Coates et al. (2022)", "https://www.sciencedirect.com/science/article/pii/S2212420921006324", "2022-01-01"),
            EducationContent(16, "Elderly Face 73% Higher Risk of Heat-Related ED Visits", ContentType.STATISTIC, EducationCategory.VULNERABILITY, "People aged 65+ face 73% higher risk of heat-related emergency visits. Check on elderly neighbours during heat events.", "VIC Population Study", "https://repository.unar.ac.id/jspui/bitstream/123456789/5756/1/11.%20Population-vulnerability-to-heat--A-case-crossover-_2023_Australian-and-New-.pdf", "2023-01-01"),
            EducationContent(17, "Cities Are Up to 7 Degrees Hotter Than Surrounding Areas", ContentType.FACT, EducationCategory.URBAN_HEAT, "Cities can be 4-7 degrees hotter than surrounding areas. Urban streets trap dangerous heat - plan your route carefully.", "ShadeMate Project", "https://www.planning.vic.gov.au/guides-and-resources/data-and-insights/cooling-and-greening-melbourne-map", "2024-01-01"),
            EducationContent(18, "Temperature Varies Up to 6 Degrees Across Melbourne Suburbs", ContentType.STATISTIC, EducationCategory.URBAN_HEAT, "Your suburb may be up to 6 degrees hotter than nearby areas. Check your local heat zone before going outside.", "Clean Air Hub", "https://www.theage.com.au/link/follow-20170101-p5csxk", "2019-01-01"),
            EducationContent(19, "Urban Greening Can Cut Temperatures by Up to 4 Degrees", ContentType.FACT, EducationCategory.URBAN_HEAT, "Green spaces and shade can cut street temperatures by up to 4 degrees. Seek tree-covered routes to stay safer.", "ScienceDirect", "https://www.sciencedirect.com/science/article/pii/S2212094718301981", "2018-01-01"),
            EducationContent(20, "Melbourne All-Time Hottest Day: 46.8 Degrees (2009)", ContentType.RECORD, EducationCategory.CLIMATE_RECORD, "Melbourne hit 46.8 degrees in 2009 - the hottest day ever recorded. Always prepare before heading outdoors in summer.", "BOM Station 086338", "https://www.extremeweatherwatch.com/cities/melbourne/highest-temperatures", "2009-02-07"),
            EducationContent(21, "Melbourne Hit 44 Degrees Twice in January 2026", ContentType.RECORD, EducationCategory.CLIMATE_RECORD, "Melbourne hit 44 degrees twice in January 2026 alone. Temperature records are being broken more frequently.", "BOM", "https://www.extremeweatherwatch.com/cities/melbourne/highest-temperatures", "2026-01-27"),
            EducationContent(22, "Melbourne Could See 61x More Heatwave Exposure by 2100", ContentType.STATISTIC, EducationCategory.FUTURE_RISK, "Melbourne could face 61 times more heatwave exposure by 2100. The time to act and prepare is now.", "Monash University", "https://research.monash.edu/en/publications/future-population-exposure-to-australian-heatwaves", "2021-01-01"),
            EducationContent(23, "Days Over 42 Degrees Have Doubled Since 2000 in Melbourne", ContentType.FACT, EducationCategory.FUTURE_RISK, "Days above 42 degrees in Melbourne have already doubled since 2000. Extreme heat is becoming the new normal.", "ABC News Australia", "https://abc.net.au/news/2026-03-12/australias-summers-are-getting-longer-with-more-extreme-heat/106419414", "2026-03-12"),
            EducationContent(24, "Heat Stress Costs Australia AU$6.9 Billion Per Year", ContentType.STATISTIC, EducationCategory.ECONOMIC_IMPACT, "Heat stress costs Australia AU$6.9 billion in lost productivity each year. Protect yourself and your team.", "Zander et al. (2015)", "https://doi.org/10.1038/nclimate2623", "2015-05-05"),
            EducationContent(25, "AU$30 Million Annual Healthcare Cost from Heat in Victoria", ContentType.STATISTIC, EducationCategory.ECONOMIC_IMPACT, "Heat costs Victoria's healthcare system AU$30 million every year. Your health is not worth the risk.", "Monash University", "https://ehjournal.biomedcentral.com/articles/10.1186/s12940-026-01289-5", "2026-03-20"),
            EducationContent(26, "Two-Thirds of Businesses Report Lower Productivity in Heat", ContentType.STATISTIC, EducationCategory.ECONOMIC_IMPACT, "2 in 3 businesses with outdoor workers report lower productivity during heatwaves. Heat affects everyone.", "Natural Hazards Research", "https://www.naturalhazards.com.au/news-and-events/news-and-views/heatwaves-cost-billions-how-we-can-better-prepare", "2026-02-04"),
            EducationContent(27, "Victoria Issued Heat Health Alerts Every Summer for 11 Years", ContentType.FACT, EducationCategory.HEALTH_IMPACT, "Victoria issued Heat Health Alerts every summer for 11 consecutive years. Heat season is every season.", "Victorian Department of Health", "https://discover.data.vic.gov.au/dataset/victorian-department-of-health-historic-heat-health-alerts-december-2010-march-2022", "2022-03-01")
        )
    }
}
