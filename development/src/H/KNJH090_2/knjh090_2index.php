<?php

require_once('for_php7.php');

require_once('knjh090_2Model.inc');
require_once('knjh090_2Query.inc');

class knjh090_2Controller extends Controller
{
    public $ModelClassName = "knjh090_2Model";
    public $ProgramID      = "KNJH090";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh090_2":
                    $sessionInstance->knjh090_2Model();
                    $this->callView("knjh090_2Form1");
                    exit;
                case "insert":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("knjh090_2");
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
$knjh090_2Ctl = new knjh090_2Controller;
