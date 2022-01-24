<?php

require_once('for_php7.php');

require_once('knja125kModel.inc');
require_once('knja125kQuery.inc');

class knja125kController extends Controller
{
    public $ModelClassName = "knja125kModel";
    public $ProgramID      = "KNJA125K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja125kForm1");
                    break 2;
                case "subform1":
                    $this->callView("knja125kSubForm1");
                    break 2;
                case "subform2":
                    $this->callView("knja125kSubForm2");
                    break 2;
                case "subform3":
                    $this->callView("knja125kSubForm3");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA125K/knja125kindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1" ."&SCHOOL_KIND=K";
                    $args["right_src"] = "knja125kindex.php?cmd=edit";
                    $args["cols"] = "23%,77%";
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
$knja125kCtl = new knja125kController();
