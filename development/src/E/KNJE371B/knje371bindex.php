<?php
require_once('knje371bModel.inc');
require_once('knje371bQuery.inc');

class knje371bController extends Controller
{
    public $ModelClassName = "knje371bModel";
    public $ProgramID      = "KNJE371B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //メイン画面
                case "":
                case "main":
                case "main_kakutei":
                case "main_edit":
                case "main_reset":
                    $this->callView("knje371bForm1");
                    break 2;
                case "main_insert":
                case "main_update":
                    list($gamen, $cmdKind) = explode("_", $sessionInstance->cmd);
                    $sessionInstance->getUpdateModelMain($cmdKind);
                    $sessionInstance->setCmd("{$gamen}_edit");
                    break 1;
                case "main_delete":
                    list($gamen, $cmdKind) = explode("_", $sessionInstance->cmd);
                    $sessionInstance->getDeleteModelMain($cmdKind);
                    $sessionInstance->setCmd("{$gamen}_edit");
                    break 1;
                case "main_copy":
                    $sessionInstance->getCopyModelMain();
                    $sessionInstance->setCmd("main_edit");
                    break 1;

                //サブ画面
                case "subCourse":
                case "subCourse_edit":
                case "subCourse_reset":
                    $this->callView("knje371bSubForm_Course");
                    break 2;
                case "subSubclass":
                case "subSubclass_edit":
                case "subSubclass_reset":
                case "subSubclass_chg":
                    $this->callView("knje371bSubForm_Subclass");
                    break 2;
                case "subQualified":
                case "subQualified_edit":
                case "subQualified_reset":
                case "subQualified_chg":
                        $this->callView("knje371bSubForm_Qualified");
                    break 2;
                case "subCourse_insert":
                case "subCourse_update":
                case "subSubclass_insert":
                case "subSubclass_update":
                case "subQualified_insert":
                case "subQualified_update":
                    list($gamen, $cmdKind) = explode("_", $sessionInstance->cmd);
                    $sessionInstance->getUpdateModelSub($cmdKind, $gamen);
                    $sessionInstance->setCmd("{$gamen}_edit");
                    break 1;
                case "subCourse_delete":
                case "subSubclass_delete":
                case "subQualified_delete":
                    list($gamen, $cmdKind) = explode("_", $sessionInstance->cmd);
                    $sessionInstance->getDeleteModelSub($cmdKind, $gamen);
                    $sessionInstance->setCmd("{$gamen}_edit");
                    break 1;

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
$knje371bCtl = new knje371bController;
