<?php

require_once('for_php7.php');

require_once('knjd644Model.inc');
require_once('knjd644Query.inc');

class knjd644Controller extends Controller {
    var $ModelClassName = "knjd644Model";
    var $ProgramID      = "KNJD644";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("rec_flg");
                    break 1;
                case "knjd644":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd644Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd644Form1");
                    exit;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "rec_flg";
                case "clear";
                    $this->callView("knjd644Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd644Ctl = new knjd644Controller;
//var_dump($_REQUEST);
?>
