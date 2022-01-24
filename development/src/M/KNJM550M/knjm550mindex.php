<?php

require_once('for_php7.php');

require_once('knjm550mModel.inc');
require_once('knjm550mQuery.inc');

class knjm550mController extends Controller
{
    public $ModelClassName = "knjm550mModel";
    public $ProgramID      = "KNJM500";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm550m_1":
                case "knjm550m_1Search":
                case "read":
                case "meibo":
                    $sessionInstance->knjm550mModel();
                    $this->callView("knjm550m_1Form1");
                    exit;
                case "knjm550m_3":
                case "knjm550m_3Search":
                case "change_class":
                    $sessionInstance->knjm550mModel();
                    $this->callView("knjm550m_3Form1");
                    exit;
                case "knjm550m_2":
                case "knjm550m_2Search":
                    $sessionInstance->knjm550mModel();
                    $this->callView("knjm550m_2Form1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm550mModel();
                    $this->callView("knjm550m_1Form1");
                    exit;
                case "csv":     //CSV出力
                    $sessionInstance->getDownloadModel();
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm550mCtl = new knjm550mController();
