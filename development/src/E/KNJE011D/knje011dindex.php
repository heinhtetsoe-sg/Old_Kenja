<?php

require_once('for_php7.php');
require_once('knje011dModel.inc');
require_once('knje011dQuery.inc');

class knje011dController extends Controller
{
    public $ModelClassName = "knje011dModel";
    public $ProgramID      = "KNJE011D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "reload3":     //RECORD_TOTALSTUDYTIME_DAT (通知書) より読込む
                case "updEdit":
                case "edit":
                case "sogo_yomikomi":
                case "select_pattern":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje011dForm1");
                    break 2;
                case "reload2_ok":  //学習指導要録より読込(OK)
                case "reload2_cancel":  //学習指導要録より読込(キャンセル)
                case "reload4":     //通知書より読込
                case "torikomi3":
                case "torikomi4":
                case "form2_first": //「出欠の～」の最初の呼出
                case "form2":       //出欠の～
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje011dForm2");
                    break 2;
                case "formSeiseki_first": //「成績参照」の最初の呼出
                case "formSeiseki":       //「成績参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje011dSubFormSeiseki");
                    break 2;
                case "formYorokuSanshou_first": //「指導要録参照」の最初の呼出
                case "formYorokuSanshou":       //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje011dSubFormYorokuSanshou");
                    break 2;
                case "formYorokuSanshou2_first": //「指導要録参照」の最初の呼出
                case "formYorokuSanshou2":       //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje011dSubFormYorokuSanshou2");
                    break 2;
                case "formShojikouTori":     //「指導上参考となる諸事項選択」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje011dSubFormShojikouTori");
                    break 2;
                case "formBikouTori":     //「備考」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje011dSubFormBikouTori");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011dForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011dForm2");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "hrShojikouTorikomi":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011dForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateHrShojikouModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "replace":
                    $this->callView("knje011dSubForm3"); //一括更新画面
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->ReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "copy_pattern":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011dForm1");
                    $sessionInstance->getCopyPatternModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "formYourokuIkkatsuTorikomi_first":
                case "formYourokuIkkatsuTorikomi":
                    $this->callView("knje011dSubFormYourokuIkkatsuTorikomi"); //指導要録所見一括取込画面
                    break 2;
                case "yourokuIkkatsuTorikomi_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje011dForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateYourokuIkkatsuModel();
                    $sessionInstance->setCmd("formYourokuIkkatsuTorikomi");
                    break 1;
                case "reset":
                    $this->callView("knje011dForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE011D/knje011dindex.php?cmd=edit") ."&button=3";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje011dindex.php?cmd=edit&init=1";
                    $args["cols"] = "23%,*";
                    View::frame($args);
                    exit;
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE011D/knje011dindex.php?cmd=edit") ."&button=3";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&GRADE={$sessionInstance->gradeHrClass}";
                    $args["right_src"] = "knje011dindex.php?cmd=edit";
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
$knje011dCtl = new knje011dController();
