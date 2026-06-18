package models

final case class UnqualifiedCompany (
    name: String,
    utr: String,
    crn: String,
    companyType: CompanyType,
    companyStatus: CompanyStatus
)

sealed trait CompanyType
object CompanyType {
  case object LTD extends CompanyType
  case object PLC extends CompanyType
}

sealed trait CompanyStatus
object CompanyStatus {
  case object Active extends CompanyStatus
  case object Administration extends CompanyStatus
  case object Dormant extends CompanyStatus
  case object Liquidation extends CompanyStatus
}
