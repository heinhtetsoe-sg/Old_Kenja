<?php

require_once('for_php7.php');

require_once('knjl379jModel.inc');
require_once('knjl379jQuery.inc');

class knjl379jController extends Controller {
	var $ModelClassName = "knjl379jModel";
    var $ProgramID      = "KNJL379J";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "init":
				case "knjl379j":
					$sessionInstance->knjl379jModel();
					$this->callView("knjl379jForm1");
					exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjl379jForm1");
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
$knjl379jCtl = new knjl379jController;
//var_dump($_REQUEST);
?>
