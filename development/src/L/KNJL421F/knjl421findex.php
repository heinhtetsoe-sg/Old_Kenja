<?php

require_once('for_php7.php');

require_once('knjl421fModel.inc');
require_once('knjl421fQuery.inc');

class knjl421fController extends Controller {
	var $ModelClassName = "knjl421fModel";
    var $ProgramID      = "KNJL421F";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl421f":
					$sessionInstance->knjl421fModel();
					$this->callView("knjl421fForm1");
					exit;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl421fCtl = new knjl421fController;
?>
