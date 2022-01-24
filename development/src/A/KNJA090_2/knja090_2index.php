<?php

require_once('for_php7.php');

require_once('knja090_2Model.inc');
require_once('knja090_2Query.inc');

class knja090_2Controller extends Controller {

    var $ModelClassName = "knja090_2Model";
    var $ProgramID      = "KNJA090";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "":
                case "sel":
                case "init":
                    $this->callView("knja090_2Form1");
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
$knja090_2_2Ctl = new knja090_2Controller;
//var_dump($_REQUEST);
?>
