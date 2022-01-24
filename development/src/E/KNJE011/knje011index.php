<?php

require_once('for_php7.php');

require_once('knje011Model.inc');
require_once('knje011Query.inc');

class knje011Controller extends Controller
{
    public $ModelClassName = "knje011Model";
    public $ProgramID      = "KNJE011";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "reload3":         //RECORD_TOTALSTUDYTIME_DAT (通知書) より読込む
                case "updEdit":
                case "edit":
                case "yomikomi":
                    $this->callView("knje011Form1");
                    break 2;
                case "reload2_ok":  //学習指導要録より読込(OK)
                case "reload2_cancel":  //学習指導要録より読込(キャンセル)
                case "reload4":         //通知書より読込
                case "reload5":         //通知書より読込
                case "torikomi3":
                case "torikomi4":
                case "form2_first":     //「出欠の～」の最初の呼出
                case "form2":           //出欠の～
                    $this->callView("knje011Form2");
                    break 2;
                case "form3_first":     //「成績参照」の最初の呼出
                case "form3":           //「成績参照」
                    $this->callView("knje011SubForm1");
                    break 2;
                case "form4_first":     //「指導要録参照」の最初の呼出
                case "form4":           //「指導要録参照」
                    $this->callView("knje011SubForm2");
                    break 2;
                case "form6_first":     //「指導要録参照」の最初の呼出
                case "form6":           //「指導要録参照」
                    $this->callView("knje011SubForm6");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011Form1");
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011Form2");
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "reload":          //保健より読み込み
                    $sessionInstance->getReloadHealthModel();
                    break 2;
                case "reset":
                    $this->callView("knje011Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == "") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE011/knje011index.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode($programpath."/knje011index.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje011index.php?cmd=edit&init=1";
                    $args["cols"] = "23%,*";
                    View::frame($args);
                    exit;
                case "back":
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == "") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE011/knje011index.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode($programpath."/knje011index.php?cmd=edit") ."&button=3" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&GRADE={$sessionInstance->gradeHrClass}";
                    $args["right_src"] = "knje011index.php?cmd=edit";
                    $args["cols"] = "23%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje011Ctl = new knje011Controller();
//var_dump($_REQUEST);
