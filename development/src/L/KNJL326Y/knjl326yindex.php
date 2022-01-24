<?php

require_once('for_php7.php');

require_once('knjl326yModel.inc');
require_once('knjl326yQuery.inc');

class knjl326yController extends Controller {
	var $ModelClassName = "knjl326yModel";
    var $ProgramID      = "KNJL326Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl326y":
					$sessionInstance->knjl326yModel();
					$this->callView("knjl326yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl326yCtl = new knjl326yController;
?>
