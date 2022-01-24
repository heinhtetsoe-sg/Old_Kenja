<?php

require_once('for_php7.php');

require_once('knjb3042Model.inc');
require_once('knjb3042Query.inc');

class knjb3042Controller extends Controller
{
    public $ModelClassName = "knjb3042Model";
    public $ProgramID      = "KNJB3042";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "editSchDiv":
                case "editCmb":
                case "getChair":
                case "getChairData":
                case "getMeiboParam":
                case "getMeiboAndFacParam":
                case "getGunCode":
                case "getFacility":
                case "getFacilitySelect":
                case "getFacilityKouzaList":
                case "getTestFacility":
                case "getTestFacilitySelect":
                case "getTestFacilityKouzaList":
                case "getLessonModeList":
                case "getLayoutStaffChair":
                case "getLayoutHrSubclass":
                case "getBackColorChair":
                case "getBackColorChairCapaOver":
                case "getBackColorChairSameMeibo":
                case "getBackColorHrClassStdMeibo":
                case "getBackColorStdChair":
                case "selectOperationHistory":
                case "getincludingChairParam":
                case "dipliStdViewParam":
                case "reset":
                    if ($sessionInstance->Properties["KNJB3042_SchTestPattern"] == "1" && $sessionInstance->field['SCH_DIV'] == '3') {
                        $this->callView("knjb3042SchTestPattern");
                    } else {
                        $this->callView("knjb3042Form1");
                    }
                    break 2;
                case "csv":
                    if (!$sessionInstance->getDownloadCSV()) {
                        $this->callView("knjb3042SchTestPattern");
                    }
                    break 2;
                case "grpform":
                    $this->callView("knjb3042grpForm1");
                    break 2;
                case "chairInfo":
                    $this->callView("knjb3042ChairInfo");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "bscseqDelete":  //テンプレート削除
                    $sessionInstance->deleteBscseqModel();
                    $sessionInstance->setCmd("");
                    break 1;

                case "updateOperationHistory":  //通知履歴更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateOperationHistoryModel();
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb3042Ctl = new knjb3042Controller();
//var_dump($_REQUEST);
