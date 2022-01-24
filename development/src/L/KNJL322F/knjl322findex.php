<?php

require_once('for_php7.php');

require_once('knjl322fModel.inc');
require_once('knjl322fQuery.inc');

class knjl322fController extends Controller {
	var $ModelClassName = "knjl322fModel";
    var $ProgramID      = "KNJL322F";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl322f":
					$sessionInstance->knjl322fModel();
					$this->callView("knjl322fForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl322fCtl = new knjl322fController;
?>
