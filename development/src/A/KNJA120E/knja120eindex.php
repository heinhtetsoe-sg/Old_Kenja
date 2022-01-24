<?php
require_once('knja120eModel.inc');
require_once('knja120eQuery.inc');

class knja120eController extends Controller
{
    public $ModelClassName = "knja120eModel";
    public $ProgramID      = "KNJA120E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "value_set":
                case "torikomi3":
                case "reload":
                case "reload2":
                case "edit":
                case "clear":
                    $this->callView("knja120eForm1");
                    break 2;
                case "subform1": //通知表所見参照
                    $this->callView("knja120eSubFormTsuchihyo");
                    break 2;
                case "subformSeisekiSansho": //成績
                    $this->callView("knja120eSubFormSeisekiSansho");
                    break 2;
                case "act_doc":  //行動の記録参照
                    $this->callView("knja120eActDoc");
                    break 2;
                case "teikei_act":
                case "teikei_val":
                    $this->callView("knja120eSubMaster");
                    break 2;
                case "tyousasyo":
                    $this->callView("knja120eTyousasyo");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "tyousasyo_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateTyousasyoModel();
                    $sessionInstance->setCmd("");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&TARGET=right_frame&PATH=" .urlencode("/A/KNJA120E/knja120eindex.php?cmd=edit") ."&button=1&SCHOOL_KIND=H";
                    $args["right_src"] = "knja120eindex.php?cmd=edit";
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
$knja120eCtl = new knja120eController();
