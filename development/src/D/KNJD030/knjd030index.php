<?php

require_once('for_php7.php');

require_once('knjd030Model.inc');
require_once('knjd030Query.inc');

class knjd030Controller extends Controller {
    var $ModelClassName = "knjd030Model";
    var $ProgramID      = "KNJD030";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "sort":
                case "cancel":
                    $sessionInstance->getMainModel();
                    $this->callView("knjd030Form1");
                   break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    //学習記録ｴｸｽﾌﾟﾛｰﾗ
                    $args["left_src"] = REQUESTROOT ."/X/KNJXGTRE/index.php?cmd=left&APPD=1&DISP=TEST&PROGRAMID=" .$this->ProgramID;
                    $args["right_src"] = "knjd030index.php?cmd=main&start=1";
                    $args["cols"] = "25%,75%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjd030Ctl = new knjd030Controller;
?>
