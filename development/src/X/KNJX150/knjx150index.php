<?php

require_once('for_php7.php');

require_once('knjx150Model.inc');
require_once('knjx150Query.inc');

class knjx150Controller extends Controller {
    var $ModelClassName = "knjx150Model";
    var $ProgramID      = "KNJX150";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":		//CSV取り込み
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
				case "csv":   //CSV出力
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjx150Form1");
					}
					break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx150Form1");
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
$knjx150Ctl = new knjx150Controller;
?>
