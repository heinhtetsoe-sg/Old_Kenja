<?php

require_once('for_php7.php');

require_once('knjxselect_staffModel.inc');
require_once('knjxselect_staffQuery.inc');

class knjxselect_staffController extends Controller
{
    public $ModelClassName = "knjxselect_staffModel";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjxselect_staff":
                    $sessionInstance->knjxselect_staffModel();
                    $this->callView("knjxselect_staff");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxselect_staffCtl = new knjxselect_staffController();
