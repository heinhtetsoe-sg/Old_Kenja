<?php

require_once('for_php7.php');

require_once('knje010dModel.inc');
require_once('knje010dQuery.inc');

class knje010dController extends Controller
{
    public $ModelClassName = "knje010dModel";
    public $ProgramID      = "KNJE010D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "updEdit":
                case "edit":
                case "yorokuYoriYomikomi_ok":
                case "yorokuYoriYomikomi_cancel":
                case "reload3":
                case "yomikomi":
                case "torikomi0":
                case "torikomi1":
                case "torikomi2":
                case "torikomiT0":
                case "torikomiT1":
                case "torikomiT2":
                case "select_pattern":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010dForm1");
                    break 2;
                case "formSeiseki_first": //「成績参照」の最初の呼出
                case "formSeiseki": //「成績参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010dSubFormSeiseki");
                    break 2;
                case "formYorokuSanshou_first": //「指導要録参照」の最初の呼出
                case "formYorokuSanshou": //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010dSubFormYorokuSanshou");
                    break 2;
                case "formYorokuSanshou2_first": //「指導要録参照」の最初の呼出
                case "formYorokuSanshou2": //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010dSubFormYorokuSanshou2");
                    break 2;
                case "formBikoTori": //「備考取込」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010dSubFormBikoTori");
                    break 2;
                case "formShojikouTori_1": //「指導上参考となる諸事項取込」
                case "formShojikouTori_2":
                case "formShojikouTori_3":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010dSubFormShojikouTori");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje010dForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "hrShojikouTorikomi":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje010dForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateHrShojikouModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "copy_pattern":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje010dForm1");
                    $sessionInstance->getCopyPatternModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "formYourokuIkkatsuTorikomi_first":
                case "formYourokuIkkatsuTorikomi":
                    $this->callView("knje010dSubFormYourokuIkkatsuTorikomi"); //指導要録所見一括取込画面
                    break 2;
                case "yourokuIkkatsuTorikomi_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje010dForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateYourokuIkkatsuModel();
                    $sessionInstance->setCmd("formYourokuIkkatsuTorikomi");
                    break 1;
                case "reset":
                    $this->callView("knje010dForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE010D/knje010dindex.php?cmd=edit") ."&button=3";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje010dindex.php?cmd=edit&init=1";
                    $args["cols"] = "22%,*";
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
$knje010dCtl = new knje010dController;
//var_dump($_REQUEST);
