<?php

require_once('for_php7.php');

require_once('knjl014cModel.inc');
require_once('knjl014cQuery.inc');

class knjl014cController extends Controller {
	var $ModelClassName = "knjl014cModel";
    var $ProgramID      = "KNJL014C";

	function main()
	{
		$sessionInstance =& Model::getModel($this);
		while ( true ) {
			switch (trim($sessionInstance->cmd)) {
				case "":
				case "knjl014c":
					$sessionInstance->knjl014cModel();
					$this->callView("knjl014cForm1");
					exit;
                case "exec":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl014c");
                    break 1;
				default:
					$sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
					$this->callView("error");
					break 2;
			}
		}
	}
}
$knjl014cCtl = new knjl014cController;
?>
