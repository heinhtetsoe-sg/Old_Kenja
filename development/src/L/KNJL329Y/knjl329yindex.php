<?php

require_once('for_php7.php');

require_once('knjl329yModel.inc');
require_once('knjl329yQuery.inc');

class knjl329yController extends Controller {
	var $ModelClassName = "knjl329yModel";
    var $ProgramID      = "KNJL329Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl329y":
				case "change":
					$sessionInstance->knjl329yModel();
					$this->callView("knjl329yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl329yCtl = new knjl329yController;
?>
