<?php

require_once('for_php7.php');

require_once('knjl327yModel.inc');
require_once('knjl327yQuery.inc');

class knjl327yController extends Controller {
	var $ModelClassName = "knjl327yModel";
    var $ProgramID      = "KNJL327Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl327y":
					$sessionInstance->knjl327yModel();
					$this->callView("knjl327yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl327yCtl = new knjl327yController;
?>
