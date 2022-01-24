<?php

require_once('for_php7.php');

require_once('knjl313cModel.inc');
require_once('knjl313cQuery.inc');

class knjl313cController extends Controller {
	var $ModelClassName = "knjl313cModel";
    var $ProgramID      = "KNJL313C";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl313c":
					$sessionInstance->knjl313cModel();
					$this->callView("knjl313cForm1");
					exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjl313cForm1");
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
$knjl313cCtl = new knjl313cController;
?>
