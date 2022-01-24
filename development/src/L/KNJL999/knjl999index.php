<?php

require_once('for_php7.php');

require_once('knjl999Model.inc');
require_once('knjl999Query.inc');

class knjl999Controller extends Controller {
    var $ModelClassName = "knjl999Model";
    var $ProgramID      = "knjl999";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":			//データ取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
				case "csv_error":   	//エラー出力
					if (!$sessionInstance->getDownloadModel()){
						$this->callView("knjl999Form1");
					}
					break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl999Form1");
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
$knjl999Ctl = new knjl999Controller;
?>
