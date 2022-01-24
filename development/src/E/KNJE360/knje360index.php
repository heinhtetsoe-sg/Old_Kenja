<?php

require_once('for_php7.php');

require_once('knje360Model.inc');
require_once('knje360Query.inc');

class knje360Controller extends Controller
{
    public $ModelClassName = "knje360Model";
    public $ProgramID      = "KNJE360";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "sort":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360Form1");
                    break 2;
                case "subform1":
                case "subform1A":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360SubForm1");
                    break 2;
                case "replace1":
                case "replace1A":
                case "replace1B":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360SubForm1_2");
                    break 2;
                case "replace_update1":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel1();
                    $sessionInstance->setCmd("replace1A");
                    break 1;
                case "subform2":
                case "subform2A":
                case "subform2B":
                case "subform2_clear":
                case "subform2_college":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360SubForm2");
                    break 2;
                case "replace2":
                case "replace2A":
                case "replace2B":
                case "replace2_college":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360SubForm2_2");
                    break 2;
                case "replace_update2":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel2();
                    $sessionInstance->setCmd("replace2A");
                    break 1;
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->chkCollegeOrCompanyMst($sessionInstance->field["STAT_CD"]);
                    // no break
                case "subform3":
                case "subform3A":
                case "subform3B":
                case "subform3_clear":
                case "kakutei_senkouno":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360SubForm3");
                    break 2;
                case "pdf":     //PDFダウンロード
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    if (!$sessionInstance->getPdfModel()) {
                        $this->callView("knje360SubForm3");
                    }
                    break 2;
                case "subform4":
                case "subform4A":
                case "subform4_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360SubForm4");
                    break 2;
                case "subform5":
                case "subform5A":
                case "subform5_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360SubForm5");
                    break 2;
                case "subform6":
                case "subform6A":
                case "subform6_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360SubForm6");
                    break 2;
                case "replace6":
                case "replace6A":
                case "replace6_search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje360SubForm6_2");
                    break 2;
                case "replace_update6":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel6();
                    $sessionInstance->setCmd("replace6A");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    break 1;
                case "subform1_insert":
                case "subform2_insert":
                case "subform3_insert":
                case "subform4_insert":
                case "subform5_insert":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "subform1_update":
                case "subform2_update":
                case "subform3_update":
                case "subform4_update":
                case "subform5_update":
                case "subform6_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE360/knje360index.php?cmd=edit") ."&button=3" ."&SES_FLG=2";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje360index.php?cmd=subform2A";
                    $args["cols"] = "22%,*";
                    View::frame($args);
                    return;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE360/knje360index.php?cmd=edit") ."&button=3" ."&SES_FLG=2";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["right_src"] = "knje360index.php?cmd=edit";
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
$knje360Ctl = new knje360Controller();
