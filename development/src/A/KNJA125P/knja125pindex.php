<?php

require_once('for_php7.php');

require_once('knja125pModel.inc');
require_once('knja125pQuery.inc');

class knja125pController extends Controller
{
    public $ModelClassName = "knja125pModel";
    public $ProgramID      = "KNJA125P";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "value_set":
                case "edit":
                case "clear":
                    $this->callView("knja125pForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "form2":
                case "clear2":
                case "behavior_semes":
                    $this->callView("knja125pForm2");
                    break 2;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "subform1":    //通知表所見参照
                    $this->callView("knja125pSubForm1");
                    break 2;
                case "subform2":    //出欠の記録参照
                    $this->callView("knja125pSubForm2");
                    break 2;
                case "teikei":
                    $this->callView("knja125pSubMaster");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA125P/knja125pindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1" ."&SCHOOL_KIND=P";
                    $args["right_src"] = "knja125pindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
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
$knja125pCtl = new knja125pController();
