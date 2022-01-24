<?php

require_once('for_php7.php');

require_once('knjc231Model.inc');
require_once('knjc231Query.inc');

class knjc231Controller extends Controller {
    var $ModelClassName = "knjc231Model";
    var $ProgramID      = "KNJC231";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->knjc231Model();     //コントロールマスタの呼び出し
                    $this->callView("knjc231Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc231Ctl = new knjc231Controller;
//var_dump($_REQUEST);
?>
