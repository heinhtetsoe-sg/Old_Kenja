<?php

require_once('for_php7.php');

require_once('knjl341fModel.inc');
require_once('knjl341fQuery.inc');

class knjl341fController extends Controller {
	var $ModelClassName = "knjl341fModel";
    var $ProgramID      = "KNJL341F";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl341f":
					$sessionInstance->knjl341fModel();
					$this->callView("knjl341fForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl341fCtl = new knjl341fController;
?>
