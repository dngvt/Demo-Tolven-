<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="Registry_Payment.xsd">
<xsl:template match="/">
<submission type="PQRI-REGISTRY" version="4.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="Registry_Payment.xsd"> 
	<file_audit_data>
		<create-date>01-23-2009</create-date>
		<create-time>23:01</create-time>
		<create-by>RegistryA</create-by>
		<version>1.0</version>
		<file-number>1</file-number>
		<number-of-files>1</number-of-files>
	</file_audit_data>
	<registry>
		<registry-name>Model Registry</registry-name>
		<registry-id>125789123</registry-id>
		<submission-method>A</submission-method>
	</registry>
	<measure-group ID="C">
		<provider>
			<npi>1257894658</npi>
			<tin>125789465</tin>
			<waiver-signed>Y</waiver-signed>
			<encounter-from-date>01-01-2009</encounter-from-date>
			<encounter-to-date>12-31-2009</encounter-to-date>
			<measure-group-stat>
				<ffs-patient-count>2</ffs-patient-count>
				<group-reporting-rate-numerator>20</group-reporting-rate-numerator>
				<group-eligible-instances>30</group-eligible-instances>
				<group-reporting-rate>100.00</group-reporting-rate>
			</measure-group-stat>
			<pqri-measure>
				<pqri-measure-number>119</pqri-measure-number>
				<collection-method>A</collection-method>
				<eligible-instances>100</eligible-instances>
				<meets-performance-instances>70</meets-performance-instances>
				<performance-exclusion-instances>20</performance-exclusion-instances>
				<performance-not-met-instances>10</performance-not-met-instances>
				<reporting-rate>100.00</reporting-rate>
				<performance-rate>88.00</performance-rate>
			</pqri-measure>
		</provider>
	</measure-group>
</submission>
</xsl:template>
</xsl:stylesheet>
