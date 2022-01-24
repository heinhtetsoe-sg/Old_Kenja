<?php

require_once('for_php7.php');

require_once('knja430s_2Model.inc');
require_once('knja430s_2Query.inc');

class knja430s_2Controller extends Controller {
    var $ModelClassName = "knja430s_2Model";
    var $ProgramID      = "KNJA430S_2";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //送信モデル呼び出し
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("inkan");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "inkan":
                    $this->callView("knja430s_2Inkan");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knja430s_2Ctl = new knja430s_2Controller;
//var_dump($_REQUEST);
?>
