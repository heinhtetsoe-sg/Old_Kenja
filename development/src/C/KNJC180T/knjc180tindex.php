<?php

require_once('for_php7.php');

require_once('knjc180tModel.inc');
require_once('knjc180tQuery.inc');

class knjc180tController extends Controller {
	var $ModelClassName = "knjc180tModel";
    var $ProgramID      = "KNJC180T";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "init":
				case "knjc180t":
					$sessionInstance->knjc180tModel();
					$this->callView("knjc180tForm1");
					exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjc180tForm1");
					}
					break 2;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjc180tCtl = new knjc180tController;
//var_dump($_REQUEST);
?>
