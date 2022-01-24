<?php

require_once('for_php7.php');

require_once('knjl320yModel.inc');
require_once('knjl320yQuery.inc');

class knjl320yController extends Controller {
	var $ModelClassName = "knjl320yModel";
    var $ProgramID      = "KNJL320Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl320y":
				case "change":
					$sessionInstance->knjl320yModel();
					$this->callView("knjl320yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl320yCtl = new knjl320yController;
?>
