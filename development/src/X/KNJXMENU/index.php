<?php

require_once('for_php7.php');
require_once('knjxmenuModel.inc');
require_once('knjxmenuQuery.inc');

class knjxmenuController extends Controller
{
    public $ModelClassName = "knjxmenuModel";
    public $ProgramID      = "KNJXMENU";     //プログラムID

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        //ログイン時
        if (VARS::post("login") == 1 && !VARS::get("kenjaMenuId")) {
            //年度変更処理
            $sessionInstance->getChgSemesModel();
        }
        if (VARS::get("kenjaMenuId") && !$sessionInstance->getSchoolWareMenuId) {
            //年度変更処理
            $sessionInstance->setSchoolWareMenuId(VARS::get("kenjaMenuId"));
        }

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                    $sessionInstance->getChgSemesModel();
                    // no break
                case "error":
                    $this->callView("error");
                    break 2;
                case "start":       //起動開始
                case "finish":      //起動終了
                    $sessionInstance->getFinishModel();
                    // no break
                case "main":
                case "change":
                case "checkNotDisp":
                case "remain":
                    $sessionInstance->getMainModel();
                    $this->callView("knjxmenuForm2");
                    break 2;
                case "online":
                    $sessionInstance->getOnlineModel();
                    break 2;
                case "send":
                    $sessionInstance->getSendModel();
                    break 2;
                case "top":
                    $sessionInstance->getTopModel();
                    break 2;
                case "top_frame":
                    $sessionInstance->getTopFrameModel();
                    break 2;
                case "top_src":
                    $sessionInstance->getTopSrcModel();
                    break 2;
                case "hidden":
                    $sessionInstance->getHiddenModel();
                    break 2;
                case "tree":
                case "retree":
                    $sessionInstance->getTreeModel();
                    $this->callView("knjxmenuForm1");
                    exit;
                    break 2;
                case "chg_pwd":
                    $this->callView("knjxmenuChg_pwd");
                    break 2;
                case "up_pwd":
                    if (!$sessionInstance->getUpPwdModel()) {
                        $this->callView("knjxmenuChg_pwd");
                    }
                    break 2;
                case "chg_year":    //年度変更処理
                    $sessionInstance->chgCtrlYear();
                    $sessionInstance->setCmd("");
                    break 1;
                case "logout":
                    header("Location: index.php?logout=true");
                    exit;
                case "misyukketu":
                    $this->callView("knjxmenuMisyukketu");
                    break 2;
                case "tuutatu":
                    $this->callView("knjxmenuTuutatu");
                    break 2;
                case "tuutatuUpd":
                    $sessionInstance->getTuutatuUpd();
                    break 2;
    //20170711 学校切替 ↓
                case "chg_school":
                    $this->callView("knjxmenuChg_school");
                    break 2;
                case "up_school":
                    header("Location: index.php?close=true");
                    exit;
    //20170711　学校切替 ↑
                case "":
                    //分割フレーム作成
                    //学習記録ｴｸｽﾌﾟﾛｰﾗ
                    $args["hidden_src"] = "index.php?cmd=hidden";
                    $args["top_src"]    = "index.php?cmd=top";
                    $args["left_src"]   = "index.php?cmd=tree";
                    $args["right_src"]  = "index.php?cmd=main";
                    $args["cols"] = "27%,*\" frameborder=\"0\"";
                    $args["rows"] = "0%,0%,*";
                    View::frame($args, "frame4.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxmenuCtl = new knjxmenuController();
