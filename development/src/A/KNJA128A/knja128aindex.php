<?php

require_once('for_php7.php');
require_once('knja128aModel.inc');
require_once('knja128aQuery.inc');

class knja128aController extends Controller
{
    public $ModelClassName = "knja128aModel";
    public $ProgramID      = "KNJA128A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "torikomi3":
                case "edit":
                case "clear":
                    $this->callView("knja128aForm1");
                    break 2;
                case "subform4": //成績
                    $this->callView("knja128aSubForm4");
                    break 2;
                case "subform6":
                    $this->callView("knja128aSubForm6");
                    break 2;
                case "subform7":
                    $this->callView("knja128aSubForm7");
                    break 2;
                case "subform8":
                    $this->callView("knja128aSubForm8");
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
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == "") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&special_div=1AND2AND3&schoolKind=A&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA128A/knja128aindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&special_div=1AND2AND3&schoolKind=A&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode($programpath."/knja128aindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}";
                    }
                    $args["right_src"] = "knja128aindex.php?cmd=edit";
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
$knja128aCtl = new knja128aController();
