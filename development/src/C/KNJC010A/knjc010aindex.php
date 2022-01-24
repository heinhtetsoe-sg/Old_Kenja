<?php

require_once('for_php7.php');
require_once('knjc010aModel.inc');
require_once('knjc010aQuery.inc');

class knjc010aController extends Controller
{
    public $ModelClassName = "knjc010aModel";
    public $ProgramID      = "KNJC010A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "syousai":
                    $this->callView("knjc010aSyousai");
                    break 2;
                case "sendKintai":
                    $sessionInstance->getSendKintai();
                    break 2;
                case "sendChair":
                    $sessionInstance->getSendChair();
                    break 2;
                case "sendKintaiInput":
                    $sessionInstance->getInputChair();
                    break 2;
                case "jugyouNaiyouAdd":
                case "resetJugyouNaiyou":
                    $this->callView("knjc010aJugyouNaiyouAdd");
                    break 2;
                case "popupInfo":
                    $this->callView("knjc010aPopupInfo");
                    break 2;
                case "main":
                case "subEnd":
                case "change":
                case "change_class":
                case "reset":
                case "hrDel":
                case "staffDel":
                case "schChrSelect":
                    $this->callView("knjc010aForm1");
                    break 2;
                case "bunkatu":
                    $this->callView("knjc010aBunkatu");
                    break 2;

                case "schChrList":
                    $this->callView("knjc010aSchChrList");
                    break 2;

                case "updateSyouSai":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModelSyouSai();
                    $sessionInstance->setCmd("syousai");
                    break 1;
                case "updateJugyouNaiyou":
                    $sessionInstance->getUpdateModelJugyouNaiyouAdd();
                    $sessionInstance->setCmd("jugyouNaiyouAdd");
                    break 1;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "bunUpd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getBunkatuUpdateModel();
                    $this->callView("knjc010aBunkatu");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}

$knjc010aCtl = new knjc010aController();
