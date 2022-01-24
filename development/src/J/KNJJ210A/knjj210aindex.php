<?php

require_once('for_php7.php');

require_once('knjj210aModel.inc');
require_once('knjj210aQuery.inc');

class knjj210aController extends Controller {
    var $ModelClassName = "knjj210aModel";
    var $ProgramID      = "KNJJ210A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj210a":                                //��˥塼���̤⤷����SUBMIT�������
                    $sessionInstance->knjj210aModel();        //����ȥ�����ޥ����θƤӽФ�
                    $this->callView("knjj210aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("̤�б��Υ��������{$sessionInstance->cmd}�Ǥ�"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj210aCtl = new knjj210aController;
var_dump($_REQUEST);
?>
