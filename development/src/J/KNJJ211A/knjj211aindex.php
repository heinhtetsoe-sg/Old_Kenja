<?php

require_once('for_php7.php');

require_once('knjj211aModel.inc');
require_once('knjj211aQuery.inc');

class knjj211aController extends Controller {
    var $ModelClassName = "knjj211aModel";
    var $ProgramID      = "KNJJ211A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj211a":                                //��˥塼���̤⤷����SUBMIT�������
                    $sessionInstance->knjj211aModel();        //����ȥ�����ޥ����θƤӽФ�
                    $this->callView("knjj211aForm1");
                    exit;
                case "csv":         //CSV�����������
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj211aForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("̤�б��Υ��������{$sessionInstance->cmd}�Ǥ�"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj211aCtl = new knjj211aController;
var_dump($_REQUEST);
?>
