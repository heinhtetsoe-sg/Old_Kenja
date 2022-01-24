<?php

require_once('for_php7.php');

require_once('knjl321yModel.inc');
require_once('knjl321yQuery.inc');

class knjl321yController extends Controller {
	var $ModelClassName = "knjl321yModel";
    var $ProgramID      = "KNJL321Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl321y":
				case "change":
					$sessionInstance->knjl321yModel();
					$this->callView("knjl321yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl321yCtl = new knjl321yController;
?>
