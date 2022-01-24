<?php

require_once("for_php7.php");

require_once('knje010eModel.inc');
require_once('knje010eQuery.inc');

class knje010eController extends Controller
{
    public $ModelClassName = "knje010eModel";
    public $ProgramID      = "KNJE010E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
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
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010eForm1");
                    break 2;
                case "formSeiseki_first": //「成績参照」の最初の呼出
                case "formSeiseki": //「成績参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010eSubFormSeiseki");
                    break 2;
                case "formYorokuSanshou_first": //「指導要録参照」の最初の呼出
                case "formYorokuSanshou": //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010eSubFormYorokuSanshou");
                    break 2;
                case "formYorokuSanshou2_first": //「指導要録参照」の最初の呼出
                case "formYorokuSanshou2": //「指導要録参照」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010eSubFormYorokuSanshou2");
                    break 2;
                case "formBikoTori": //「備考取込」
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010eSubFormBikoTori");
                    break 2;
                case "formShojikouTori_1": //「指導上参考となる諸事項取込」
                case "formShojikouTori_2":
                case "formShojikouTori_3":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje010eSubFormShojikouTori");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje010eForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "hrShojikouTorikomi":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knje010eForm1");
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateHrShojikouModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "reset":
                    $this->callView("knje010eForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE010E/knje010eindex.php?cmd=edit") ."&button=3";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje010eindex.php?cmd=edit&init=1";
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
$knje010eCtl = new knje010eController();
//var_dump($_REQUEST);
