<?php

require_once('for_php7.php');

require_once('knjl321fModel.inc');
require_once('knjl321fQuery.inc');

class knjl321fController extends Controller {
	var $ModelClassName = "knjl321fModel";
    var $ProgramID      = "KNJL321F";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl321f":
					$sessionInstance->knjl321fModel();
					$this->callView("knjl321fForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl321fCtl = new knjl321fController;
?>
