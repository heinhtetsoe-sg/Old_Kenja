<?php

require_once('for_php7.php');

require_once('knjx030bModel.inc');
require_once('knjx030bQuery.inc');

class knjx030bController extends Controller {
    var $ModelClassName = "knjx030bModel";
    var $ProgramID      = "KNJX030B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":	//CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
				case "csv":   	//CSV出力
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjx030bForm1");
					}
					break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx030bForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjx030bCtl = new knjx030bController;
?>
