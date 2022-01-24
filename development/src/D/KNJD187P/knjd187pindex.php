<?php

require_once('for_php7.php');

require_once('knjd187pModel.inc');
require_once('knjd187pQuery.inc');

class knjd187pController extends Controller
{
    public $ModelClassName = "knjd187pModel";
    public $ProgramID      = "KNJD187P";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd187p":
                case "knjd187pChangeSemester":
                    $sessionInstance->knjd187pModel();
                    $this->callView("knjd187pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187pCtl = new knjd187pController();
