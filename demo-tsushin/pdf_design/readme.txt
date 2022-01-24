
|-- src_xml
|   |-- KNJD186V_A.fld.xml
|   |-- KNJD186V_A.xml
|   |-- serial
|   |   |-- KNJD186V_A.bin
|   |   |-- KNJD186V_A.fld.bin
|   |-- template
|   |   |-- KNJD186V_A_Template.pdf
|   `-- xml -> ../template_xml
|
|-- mod_xml
|   |-- serial
|   |   |-- KNJD186V_A.bin
|   |   `-- KNJD186V_A.fld.bin
|   |-- template
|   |   `-- KNJD186V_A_Template.pdf
|   `-- xml -> ../xml
|
|-- template_xml
|   |-- KNJD186V_A.fld.xml
|   |-- KNJD186V_A.xml
|
`-- xml

KNJX251 アルプPDFデザインフォーム送受信
PCにtemplate_xmlのテンプレートフォームをコピーして
xmlに修正したフォームをコピーします。

各帳票(pdf.AlpPdf)
mod_xml、 src_xmlの順にフォームを探します。
mod_xml、 src_xmlのserial、templateはファイルがなければ出力時に作成します。

