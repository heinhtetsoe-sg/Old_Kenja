<?php

require_once('for_php7.php');

require_once('knjj081Model.inc');
require_once('knjj081Query.inc');

class knjj081Controller extends Controller {
    var $ModelClassName = "knjj081Model";
    var $ProgramID      = "KNJJ081";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
//                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("grp_meb");
                    break 1;
                case "knjj081Form1":                                //メニュー画面もしくはSUBMITした場合
//                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjj081Model();        //コントロールマスタの呼び出し
                    $this->callView("knjj081Form1");
                    exit;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "grp_meb";
                case "clear";
//                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj081Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj081Ctl = new knjj081Controller;
//var_dump($_REQUEST);
?>
