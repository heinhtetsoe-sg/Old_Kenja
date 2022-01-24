<?php

require_once('for_php7.php');

require_once('knjd210Model.inc');
require_once('knjd210Query.inc');

class knjd210Controller extends Controller {
    var $ModelClassName = "knjd210Model";
    var $ProgramID      = "KNJD210";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "estimate":          //仮評再処理
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getEstimateModel();
                case "sort":
                case "main":
                    $sessionInstance->getMainModel();
                case "cancel":
                    $this->callView("knjd210Form1");
                   break 2;
                case "avg":             //平均点補正処理
                    $sessionInstance->getAvgModel();
                    //平均点補正処理
                    $this->callView("knjd210Form2");
                    break 2;
                case "exec":             //相対評価処理実行
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    break 2;
                case "read":             //相対評価処理
                case "assess":             //相対評価処理
                    $this->callView("knjd210Form3");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    //学習記録ｴｸｽﾌﾟﾛｰﾗ
                    $args["left_src"] = REQUESTROOT ."/X/KNJXGTRE/index.php?cmd=left&APPD=1&DISP=CLASS&PROGRAMID=" .$this->ProgramID;
                    $args["right_src"] = "knjd210index.php?cmd=main&start=1";
                    $args["cols"] = "10%,*%";
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
$knjd210Ctl = new knjd210Controller;
?>
