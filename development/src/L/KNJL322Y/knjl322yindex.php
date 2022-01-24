<?php

require_once('for_php7.php');

require_once('knjl322yModel.inc');
require_once('knjl322yQuery.inc');

class knjl322yController extends Controller {
	var $ModelClassName = "knjl322yModel";
    var $ProgramID      = "KNJL322Y";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl322y":
					$sessionInstance->knjl322yModel();
					$this->callView("knjl322yForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl322yCtl = new knjl322yController;
?>
