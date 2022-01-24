<?php

require_once('for_php7.php');

require_once('knja128pModel.inc');
require_once('knja128pQuery.inc');

class knja128pController extends Controller
{
    public $ModelClassName = "knja128pModel";
    public $ProgramID      = "KNJA128P";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knja128pForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "form2":
                case "clear2":
                    $this->callView("knja128pForm2");
                    break 2;
                case "update2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "teikei":
                    $this->callView("knja128pSubMaster");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&special_div=1AND2AND3&schoolKind=P&TARGET=right_frame&PATH=" .urlencode("/A/KNJA128P/knja128pindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1&HANDICAP_FLG=N1";
                    $args["right_src"] = "knja128pindex.php?cmd=edit";
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
$knja128pCtl = new knja128pController();
