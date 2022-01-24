<?php

require_once('for_php7.php');

require_once('knje360jModel.inc');
require_once('knje360jQuery.inc');

class knje360jController extends Controller
{
    public $ModelClassName = "knje360jModel";
    public $ProgramID      = "KNJE360J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360jForm1");
                    break 2;
                case "shinro":
                case "shinroA":
                case "shinro_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360jShinro");
                    break 2;
                case "replace1":
                case "replace1A":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360jShinro_2");
                    break 2;
                case "replace_update1":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->ReplaceModel1();
                    $sessionInstance->setCmd("replace1A");
                    break 1;
                case "shingaku":
                case "shingakuA":
                case "shingaku_clear":
                case "shingaku_college":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360jShingaku");
                    break 2;
                case "replace2":
                case "replace2A":
                case "replace2B":
                case "replace2_college":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360jShingaku_2");
                    break 2;
                case "replace_update2":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->ReplaceModel2();
                    $sessionInstance->setCmd("replace2A");
                    break 1;
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->chkCollegeOrCompanyMst($sessionInstance->field["STAT_CD"]);
                    // no break
                case "syuusyoku":
                case "syuusyokuA":
                case "syuusyoku_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360jSyuusyoku");
                    break 2;
                case "pdf":     //PDFダウンロード
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    if (!$sessionInstance->getPdfModel()) {
                        $this->callView("knje360jSyuusyoku");
                    }
                    break 2;
                case "shinroSoudan":
                case "shinroSoudanA":
                case "shinroSoudan_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360jShinroSoudan");
                    break 2;
                case "sonota":
                case "sonotaA":
                case "sonota_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360jSonota");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    break 1;
                case "shinro_insert":
                case "shingaku_insert":
                case "syuusyoku_insert":
                case "shinroSoudan_insert":
                case "sonota_insert":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "shinro_update":
                case "shingaku_update":
                case "syuusyoku_update":
                case "shinroSoudan_update":
                case "sonota_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE360J/knje360jindex.php?cmd=edit") ."&button=3" ."&SES_FLG=2";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje360jindex.php?cmd=shingakuA";
                    $args["cols"] = "22%,*";
                    View::frame($args);
                    return;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE360J/knje360jindex.php?cmd=edit") ."&button=3" ."&SES_FLG=2";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje360jindex.php?cmd=edit";
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
$knje360jCtl = new knje360jController;
